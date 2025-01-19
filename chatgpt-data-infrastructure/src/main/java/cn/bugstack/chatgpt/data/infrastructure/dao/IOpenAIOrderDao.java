package cn.bugstack.chatgpt.data.infrastructure.dao;

import cn.bugstack.chatgpt.data.infrastructure.po.OpenAIOrderPO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface IOpenAIOrderDao {
//    查询未支付订单
    OpenAIOrderPO queryUnpaidOrder(OpenAIOrderPO openAIOrderPOReq);


    void insert(OpenAIOrderPO openAIOrderPO);

    void updateOrderPayInfo(OpenAIOrderPO openAIOrderPO);

    boolean changeOrderPaySuccess(OpenAIOrderPO openAIOrderPO);
}
