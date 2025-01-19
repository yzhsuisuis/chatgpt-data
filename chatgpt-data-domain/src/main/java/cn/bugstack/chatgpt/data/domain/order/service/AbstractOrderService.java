package cn.bugstack.chatgpt.data.domain.order.service;

import cn.bugstack.chatgpt.data.domain.auth.service.AbstractAuthService;
import cn.bugstack.chatgpt.data.domain.order.model.entity.*;
import cn.bugstack.chatgpt.data.domain.order.model.valobj.PayStatusVO;
import cn.bugstack.chatgpt.data.domain.order.repository.IOrderRepository;
import cn.bugstack.chatgpt.data.types.common.Constants;
import cn.bugstack.chatgpt.data.types.exception.ChatGPTException;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;
import java.io.IOException;
import java.math.BigDecimal;

@Slf4j
public abstract class AbstractOrderService implements IOrderService{
    @Resource
    private IOrderRepository orderRepository;
    @Override
    public PayOrderEntity createOrder(ShopCartEntity shopCartEntity)  {
        try {
            String openid = shopCartEntity.getOpenid();
            Integer productId = shopCartEntity.getProductId();

            UnpaidOrderEntity unpaidOrderEntity = orderRepository.queryUnpaidOrder(shopCartEntity);
            /*
            * 未支付的订单也分2种
            * 1. 已经生成的payurl的-----> 直接返回就行了
            * 2. 未生成payurl的 ------> 需要执行一遍doOrder()
            * */
            if(null != unpaidOrderEntity && PayStatusVO.WAIT.equals(unpaidOrderEntity.getPayStatus()) && null != unpaidOrderEntity.getPayUrl())
            {
                log.info("创建订单-存在，已生成微信支付，返回 openid: {} orderId: {} payUrl: {}", openid, unpaidOrderEntity.getOrderId(), unpaidOrderEntity.getPayUrl());
                return PayOrderEntity.builder()
                        .openid(openid)
                        .orderId(unpaidOrderEntity.getOrderId())
                        .payUrl(unpaidOrderEntity.getPayUrl())
                        .payStatus(unpaidOrderEntity.getPayStatus())
                        .build();
            } else if (null != unpaidOrderEntity && null == unpaidOrderEntity.getPayUrl()) {
                log.info("创建订单-存在，未生成微信支付，返回 openid: {} orderId: {}", openid, unpaidOrderEntity.getOrderId());
                PayOrderEntity payOrderEntity = this.doPrepayOrder(openid, unpaidOrderEntity.getOrderId(), unpaidOrderEntity.getProductName(), unpaidOrderEntity.getTotalAmount());
                log.info("创建订单-完成，生成支付单。openid: {} orderId: {} payUrl: {}", openid, payOrderEntity.getOrderId(), payOrderEntity.getPayUrl());
                return payOrderEntity;
            }
//        还没有创建对应的订单,先创建订单,再保存订单
//            1.根据productid来查询商品名称
            ProductEntity productEntity = orderRepository.queryProductById(productId);
            if(!productEntity.isAvailable())
            {
                throw new ChatGPTException(Constants.ResponseCode.ORDER_PRODUCT_ERR.getCode(),Constants.ResponseCode.ORDER_PRODUCT_ERR.getInfo());
            }

//            保存订单
             OrderEntity orderEntity = this.doSaveOrder(openid,productEntity);
//            2. 根据创建订单的orderid ,然后在prepay时进行更改订单的状态
//            生成支付码,并再次更替订单的状态
            PayOrderEntity payOrderEntity = this.doPrepayOrder(openid, orderEntity.getOrderId(), productEntity.getProductName(), orderEntity.getTotalAmount());

            log.info("创建订单-完成，生成支付单。openid: {} orderId: {} payUrl: {}", openid, orderEntity.getOrderId(), payOrderEntity.getPayUrl());

            return payOrderEntity;


        } catch (Exception e) {
            log.error("创建订单，已生成微信支付，返回 openid: {} productId: {}", shopCartEntity.getOpenid(), shopCartEntity.getProductId());
            throw new ChatGPTException(Constants.ResponseCode.UN_ERROR.getCode(), Constants.ResponseCode.UN_ERROR.getInfo());
        }

    }

    protected abstract OrderEntity doSaveOrder(String openid, ProductEntity productEntity) ;


    protected abstract PayOrderEntity doPrepayOrder(String openid, String orderId, String productName, BigDecimal totalAmount) throws IOException;

}
