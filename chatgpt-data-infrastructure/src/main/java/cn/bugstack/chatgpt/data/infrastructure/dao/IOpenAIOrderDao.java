package cn.bugstack.chatgpt.data.infrastructure.dao;

import cn.bugstack.chatgpt.data.infrastructure.po.OpenAIOrderPO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface IOpenAIOrderDao {
//    查询未支付订单
    OpenAIOrderPO queryUnpaidOrder(OpenAIOrderPO openAIOrderPOReq);


    void insert(OpenAIOrderPO openAIOrderPO);

    void updateOrderPayInfo(OpenAIOrderPO openAIOrderPO);

    boolean changeOrderPaySuccess(OpenAIOrderPO openAIOrderPO);

    OpenAIOrderPO queryOrder(String orderId);

    int updateOrderStatusDeliverGoods(String orderId);

    List<String> queryTimeoutCloseOrderList();

    boolean changeOrderClose(String orderId);

    List<String> queryNoPayNotifyOrder();

    List<String> queryReplenishmentOrder();
}
