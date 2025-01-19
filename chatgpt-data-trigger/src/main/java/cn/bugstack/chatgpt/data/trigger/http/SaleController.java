package cn.bugstack.chatgpt.data.trigger.http;

import cn.bugstack.chatgpt.data.domain.auth.service.IAuthService;
import cn.bugstack.chatgpt.data.domain.order.model.entity.PayOrderEntity;
import cn.bugstack.chatgpt.data.domain.order.model.entity.ShopCartEntity;
import cn.bugstack.chatgpt.data.domain.order.service.IOrderService;
import cn.bugstack.chatgpt.data.types.common.Constants;
import cn.bugstack.chatgpt.data.types.model.Response;
import cn.bugstack.ltzf.payments.nativepay.NativePayService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
@RestController()
@CrossOrigin("${app.config.cross-origin}")
@RequestMapping("/api/${app.config.api-version}/sale/")
public class SaleController {
    @Resource
    private IAuthService authService;
    @Resource
    private IOrderService orderService;
    @Resource
    private NativePayService nativePayService;


    @RequestMapping(value = "create_pay_order", method = RequestMethod.POST)
    public Response<String> createParOrder(@RequestHeader("Authorization") String token, @RequestParam Integer productId) {

        try {
            // 1. Token 校验
            boolean success = authService.checkToken(token);
            if (!success) {
                return Response.<String>builder()
                        .code(Constants.ResponseCode.TOKEN_ERROR.getCode())
                        .info(Constants.ResponseCode.TOKEN_ERROR.getInfo())
                        .build();
            }
            // 2. Token 解析
            String openid = authService.openid(token);
            assert null != openid;
            log.info("用户商品下单，根据商品ID创建支付单开始 openid:{} productId:{}", openid, productId);
            ShopCartEntity shopCartEntity = ShopCartEntity.builder()
                    .openid(openid)
                    .productId(productId)
                    .build();
            PayOrderEntity payOrder = orderService.createOrder(shopCartEntity);
            log.info("用户商品下单，根据商品ID创建支付单完成 openid: {} productId: {} orderPay: {}", openid, productId, payOrder.toString());

            return Response.<String>builder()
                    .code(Constants.ResponseCode.SUCCESS.getCode())
                    .info(Constants.ResponseCode.SUCCESS.getInfo())
                    .data(payOrder.getPayUrl())
                    .build();
        } catch (Exception e) {
            log.error("用户商品下单，根据商品ID创建支付单失败", e);
            return Response.<String>builder()
                    .code(Constants.ResponseCode.UN_ERROR.getCode())
                    .info(Constants.ResponseCode.UN_ERROR.getInfo())
                    .build();
        }
    }
    /*

   当用户付完钱后,会回调下面的接口
   回调返回值为:
   code=0
   &timestamp=1737271595
   &mch_id=1700833091
   &order_no=WX202501191525307675341724
   &out_trade_no=38746363
   &pay_no=4200002539202501198057114787
   &total_fee=0.11&sign=27E83A40D365C02BDEC22956DBE60F6A
   &pay_channel=wxpay
   &trade_type=NATIVE
   &success_time=2025-01-19+15%3A26%3A35&attach=
   &openid=o5wq46MQhyUA5ZaPxy0-Wx4fjhRQ
    * */

    @RequestMapping(value = "pay_notify", method = RequestMethod.POST)
    public String payNotify(
            @RequestParam String code,
            @RequestParam String timestamp,
            @RequestParam String mch_id,
            @RequestParam String order_no,
            @RequestParam String out_trade_no,
            @RequestParam String pay_no,
            @RequestParam String total_fee,
            @RequestParam(required = false) String sign,
            @RequestParam(required = false) String pay_channel,
            @RequestParam(required = false) String trade_type,
            @RequestParam(required = false) String success_time,
            @RequestParam(required = false) String attach,
            @RequestParam(required = false) String openid,
            HttpServletRequest request){
        try {
            log.info("code: {}", code);
            log.info("timestamp: {}", timestamp);
            log.info("mch_id: {}", mch_id);
            log.info("order_no: {}", order_no);
            log.info("out_trade_no: {}", out_trade_no);
            log.info("pay_no: {}", pay_no);
            log.info("total_fee: {}", total_fee);
            log.info("sign: {}", sign);
            log.info("pay_channel: {}", pay_channel);
            log.info("trade_type: {}", trade_type);
            log.info("success_time: {}", success_time);
            log.info("attach: {}", attach);
            log.info("openid: {}", openid);
            //3个点需要再重新填上
            //1. payamount
            //2. transaction_id
            //3.pay_time(可以直接填上)
            /*code: 0
              timestamp: 1737273234
              mch_id: 1700833091
              order_no: WX202501191539181849131422
              out_trade_no: 98812297
              pay_no: 4200002626202501192238550801
              total_fee: 0.11
              sign: 5DEDA50295C94FFF0765D6D26A6E5967
              pay_channel: wxpay
              trade_type: NATIVE
              success_time: 2025-01-19 15:39:51
              attach:
              openid: o5wq46MQhyUA5ZaPxy0-Wx4fjhRQ

*/
            if("0".equals(code))
            {
                log.info("订单支付成功 orderId:{} total_fee:{} successTime:{}",out_trade_no,total_fee,success_time);
                boolean isSuccess = orderService.changeOrderPaySuccess(out_trade_no,total_fee,order_no,success_time);
                if(isSuccess)
                {
                    log.info("订单修改完成");
                }
            }
            return "SUCCESS";
        } catch (Exception e) {
            return "FAIL";
        }


    }



}
