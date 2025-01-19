package cn.bugstack.chatgpt.data.domain.order.service;

import cn.bugstack.chatgpt.data.domain.order.model.aggregates.CreateOrderAggregate;
import cn.bugstack.chatgpt.data.domain.order.model.entity.OrderEntity;
import cn.bugstack.chatgpt.data.domain.order.model.entity.PayOrderEntity;
import cn.bugstack.chatgpt.data.domain.order.model.entity.ProductEntity;
import cn.bugstack.chatgpt.data.domain.order.model.valobj.OrderStatusVO;
import cn.bugstack.chatgpt.data.domain.order.model.valobj.PayStatusVO;
import cn.bugstack.chatgpt.data.domain.order.model.valobj.PayTypeVO;
import cn.bugstack.chatgpt.data.domain.order.repository.IOrderRepository;
import cn.bugstack.ltzf.payments.nativepay.NativePayService;
import cn.bugstack.ltzf.payments.nativepay.model.PrepayRequest;
import cn.bugstack.ltzf.payments.nativepay.model.PrepayResponse;
import com.alibaba.fastjson.JSON;
import com.wechat.pay.java.service.payments.app.model.Amount;


import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.Date;

@Slf4j
@Service
public class OrderService extends AbstractOrderService{
    @Value("${ltzf.sdk.config.app_id}")
    private String appid;
    @Value("${ltzf.sdk.config.merchant_id}")
    private String mchid;
    @Value("${ltzf.sdk.config.notify-url}")
    private String notifuUrl;

    @Resource
    private IOrderRepository orderRepository;
    @Resource
    private NativePayService nativePayService;
    @Override
    protected OrderEntity doSaveOrder(String openid, ProductEntity productEntity) {
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setOrderTime(new Date());
        orderEntity.setOrderId(RandomStringUtils.randomNumeric(8));
        orderEntity.setPayTypeVO(PayTypeVO.WEIXIN_NATIVE);
        orderEntity.setTotalAmount(productEntity.getPrice());
        orderEntity.setOrderStatus(OrderStatusVO.CREATE);
        // 保存订单；订单和支付，是2个操作。
        // 一个是数据库操作，一个是HTTP操作。所以不能一个事务处理，只能先保存订单再操作创建支付单，如果失败则需要任务补偿
        CreateOrderAggregate createOrderAggregate = CreateOrderAggregate.builder()
                .openId(openid)
                .orderEntity(orderEntity)
                .productEntity(productEntity)
                .build();

        orderRepository.saveOrder(createOrderAggregate);
        return orderEntity;
    }

    @Override
    protected PayOrderEntity doPrepayOrder(String openid, String orderId, String productName, BigDecimal totalAmount) throws IOException {
        // 1. 请求参数
        PrepayRequest request = new PrepayRequest();
        request.setMchid(mchid);
        request.setOutTradeNo(orderId);
        request.setTotalFee(totalAmount.toString());
        request.setBody(productName);
        request.setNotifyUrl(notifuUrl);

        // 2. 创建支付订单

        String codeUrl = "";
        if (null != nativePayService) {
            PrepayResponse response = nativePayService.prePay(request);
            codeUrl = response.getData().getCodeUrl();
//            codeUrl = prepay.getCodeUrl();
        } else {
            codeUrl = "因你未配置支付渠道，所以暂时不能生成有效的支付URL。请配置支付渠道后，在application-dev.yml中配置支付渠道信息";
        }

        PayOrderEntity payOrderEntity = PayOrderEntity.builder()
                .openid(openid)
                .orderId(orderId)
                .payUrl(codeUrl)
                .payStatus(PayStatusVO.WAIT)
                .build();


        // 更新订单支付信息
        orderRepository.updateOrderPayInfo(payOrderEntity);
        return payOrderEntity;
    }

    @Override
    public boolean changeOrderPaySuccess(String out_trade_no, String total_fee, String order_no, String success_time) throws ParseException {

        return orderRepository.changeOrderPaySuccess(out_trade_no,total_fee,order_no,success_time);
    }
}
