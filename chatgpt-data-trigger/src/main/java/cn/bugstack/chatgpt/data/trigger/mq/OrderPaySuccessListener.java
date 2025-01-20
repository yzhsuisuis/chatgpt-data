package cn.bugstack.chatgpt.data.trigger.mq;

import cn.bugstack.chatgpt.data.domain.order.service.OrderService;
import com.google.common.eventbus.Subscribe;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;


@Slf4j
@Component
public class OrderPaySuccessListener {
    @Resource
    private OrderService orderService;

    @Subscribe
    public void handleEvent(String orderId) {
        try {
            log.info("支付完成，发货并记录，开始。订单：{}", orderId);
            orderService.deliverGoods(orderId);
        } catch (Exception e) {
            log.error("支付完成，发货并记录，失败。订单：{}", orderId, e);
        }
    }

}
