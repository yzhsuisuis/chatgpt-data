package cn.bugstack.chatgpt.data.trigger.job;

import cn.bugstack.chatgpt.data.domain.order.model.valobj.PayStatusVO;
import cn.bugstack.chatgpt.data.domain.order.service.IOrderService;
import cn.bugstack.ltzf.payments.nativepay.NativePayService;
import cn.bugstack.ltzf.payments.nativepay.model.QueryOrderByOutTradeNoRequest;
import cn.bugstack.ltzf.payments.nativepay.model.QueryOrderByOutTradeNoResponse;
import com.google.common.eventbus.EventBus;
import com.wechat.pay.java.service.payments.model.Transaction;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * @Author: yangzihe
 * @CreateTime: 2025/01/19  19:30
 * @Description:
 */
@Slf4j
@Component
public class NoPayNotifyOrderJob {
    @Resource
    private IOrderService orderService;
    @Autowired(required = false)
    private NativePayService payService;

    @Value("${ltzf.sdk.config.merchant_id}")
    private String mchid;

    @Resource
    private EventBus eventBus;

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");

    @Scheduled(cron = "0 0/1 * * * ?")
    public void exec() {
        try {
            //    查询没有回调的单子,先用Order查询本地的,
            //    查询条件为: order_status = 0 pay_status = 0  now() > orderTime + 1 minute
            List<String> orderIds = orderService.queryNoPayNotifyOrder();
            for (String orderId : orderIds) {
                //这里使用蓝兔支付自己封装的一个包
                QueryOrderByOutTradeNoRequest request = new QueryOrderByOutTradeNoRequest();
                request.setMchid(mchid);
                request.setOutTradeNo(orderId);
                QueryOrderByOutTradeNoResponse queryOrderByOutTradeNoResponse = payService.QueryOrderByOutTradeNO(request);
                Long code = queryOrderByOutTradeNoResponse.getCode();
                if (code == 0) {
                    //    code == 1的话,代表查询成功
                    //    查询的订单结果如果 pay_status = 0 ,则代表还没有支付,那就继续等就完了,等超过30分钟会自动关单的
                    if (PayStatusVO.WAIT.getCode().equals(queryOrderByOutTradeNoResponse.getData().getPayStatus())) {
                        //    此时代表该订单只是没支付,并没有掉单
                        log.info("订单orderId:{},尚未支付", orderId);
                        continue;
                    }
                    //    如果走到这一步则代表对方付了钱,但是却没有更新状态,相当于掉单了,则重新修改,本地就可以了(人家支付的那一方肯定是没啥问题的)
                    QueryOrderByOutTradeNoResponse.Data data = queryOrderByOutTradeNoResponse.getData();
                    //要修改的地方,就是创建完订单,到支付完,补充的那几个字段
                    String orderNo = data.getOrderNo();
                    //这是一个很坑的点,outTradeNo相当于orderId ,是用户自己传递给他的,而OderNo是回调生成的
                    //String outTradeNo = data.getOutTradeNo();
                    String totalFee = data.getTotalFee();
                    boolean isSuccess = orderService.changeOrderPaySuccess(orderId,totalFee, orderNo, data.getSuccessTime());
                    if (isSuccess) {
                        // 发布消息
                        eventBus.post(orderId);
                    }


                }


            }
        } catch (Exception e) {
            log.error("定时任务，订单支付状态更新失败", e);
        }


    }

}
