package cn.bugstack.chatgpt.data.domain.order.service;

import cn.bugstack.chatgpt.data.domain.order.model.entity.PayOrderEntity;
import cn.bugstack.chatgpt.data.domain.order.model.entity.ProductEntity;
import cn.bugstack.chatgpt.data.domain.order.model.entity.ShopCartEntity;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;

public interface IOrderService {


//    创建订单
    PayOrderEntity createOrder(ShopCartEntity shopCartEntity) throws IOException;

    boolean changeOrderPaySuccess(String out_trade_no, String total_fee, String order_no, String success_time) throws ParseException;

    void deliverGoods(String orderId);

    List<String> queryTimeoutCloseOrderList();

    boolean changeOrderClose(String orderId);

    List<String> queryNoPayNotifyOrder();

    List<String> queryReplenishmentOrder();

    List<ProductEntity> queryProductList();

    String queryUserAccountQuota(String openid);
}
