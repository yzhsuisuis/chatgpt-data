package cn.bugstack.chatgpt.data.domain.order.repository;

import cn.bugstack.chatgpt.data.domain.order.model.aggregates.CreateOrderAggregate;
import cn.bugstack.chatgpt.data.domain.order.model.entity.PayOrderEntity;
import cn.bugstack.chatgpt.data.domain.order.model.entity.ProductEntity;
import cn.bugstack.chatgpt.data.domain.order.model.entity.ShopCartEntity;
import cn.bugstack.chatgpt.data.domain.order.model.entity.UnpaidOrderEntity;

import java.text.ParseException;

/**
 * @author Fuzhengwei bugstack.cn @小傅哥
 * @description 订单仓储接口
 * @create 2023-10-05 13:11
 */
public interface IOrderRepository {


    UnpaidOrderEntity queryUnpaidOrder(ShopCartEntity shopCartEntity);


    ProductEntity queryProductById(Integer productId);

    void saveOrder(CreateOrderAggregate createOrderAggregate);

    void updateOrderPayInfo(PayOrderEntity payOrderEntity);

    boolean changeOrderPaySuccess(String out_trade_no, String total_fee, String order_no, String success_time) throws ParseException;
}
