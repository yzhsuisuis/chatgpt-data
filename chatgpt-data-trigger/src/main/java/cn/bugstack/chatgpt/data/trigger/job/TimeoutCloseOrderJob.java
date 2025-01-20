package cn.bugstack.chatgpt.data.trigger.job;

import cn.bugstack.chatgpt.data.domain.order.service.IOrderService;
import cn.bugstack.ltzf.payments.nativepay.NativePayService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Author: yangzihe
 * @CreateTime: 2025/01/19  19:09
 * @Description:超时关单,和监听器listener没有任何关系
 */
@Slf4j
@Component
public class TimeoutCloseOrderJob {
    @Resource
    private IOrderService orderService;
    @Autowired(required = false)
    private NativePayService payService;

    //定时任务
    @Scheduled(cron = "0 0/10 * * * ?")
    public void exec() {
        try {
            if(null == payService)
            {
                log.info("定时任务，订单支付状态更新。应用未配置支付渠道，任务不执行。");
                return;
            }
            List<String> orderIds = orderService.queryTimeoutCloseOrderList();

            for (String orderId : orderIds) {
                boolean status = orderService.changeOrderClose(orderId);

                log.info("定时任务，超时30分钟订单关闭 orderId: {} status：{}", orderId, status);
            }
        } catch (Exception e) {
            log.error("定时任务，超时15分钟订单关闭失败", e);
        }

    }


}
