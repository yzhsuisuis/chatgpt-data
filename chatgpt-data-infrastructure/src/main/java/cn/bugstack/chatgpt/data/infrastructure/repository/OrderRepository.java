package cn.bugstack.chatgpt.data.infrastructure.repository;

import cn.bugstack.chatgpt.data.domain.order.model.aggregates.CreateOrderAggregate;
import cn.bugstack.chatgpt.data.domain.order.model.entity.*;
import cn.bugstack.chatgpt.data.domain.order.model.valobj.PayStatusVO;
import cn.bugstack.chatgpt.data.domain.order.repository.IOrderRepository;
import cn.bugstack.chatgpt.data.infrastructure.dao.IOpenAIOrderDao;
import cn.bugstack.chatgpt.data.infrastructure.dao.IOpenAIProductDao;
import cn.bugstack.chatgpt.data.infrastructure.po.OpenAIOrderPO;
import cn.bugstack.chatgpt.data.infrastructure.po.OpenAIProductPO;
import cn.bugstack.chatgpt.data.types.enums.OpenAIProductEnableModel;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

@Repository
public class OrderRepository implements IOrderRepository {
    @Resource
    private IOpenAIOrderDao openAIOrderDao;

    @Resource
    private IOpenAIProductDao openAIProductDao;

    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");


    @Override
    public UnpaidOrderEntity queryUnpaidOrder(ShopCartEntity shopCartEntity) {
//        查询Order的时候是不清楚,订单号的
        String openid = shopCartEntity.getOpenid();
        Integer productId = shopCartEntity.getProductId();
        OpenAIOrderPO openAIOrderReq = OpenAIOrderPO.builder()
                .openid(openid)
                .productId(productId).build();


        OpenAIOrderPO openAIOrderPORes = openAIOrderDao.queryUnpaidOrder(openAIOrderReq);
        if(null == openAIOrderPORes) return null;
        return UnpaidOrderEntity.builder()
                .openid(openid)
                .orderId(openAIOrderPORes.getOrderId())
                .payUrl(openAIOrderPORes.getPayUrl())
                .productName(openAIOrderPORes.getProductName())
                .totalAmount(openAIOrderPORes.getTotalAmount())
//                这里是很细节的地方,这里的从po往Vo转化的过程中,会通过枚举类,将存在数据库里的裸数据,转化成Type类型的
                .payStatus(PayStatusVO.get(openAIOrderPORes.getPayStatus()))
                .build();
    }

    @Override
    public ProductEntity queryProductById(Integer productId) {
        OpenAIProductPO openAIProductPO = openAIProductDao.queryProductByProductId(productId);
        ProductEntity productEntity = new ProductEntity();
        productEntity.setProductId(openAIProductPO.getProductId());
        productEntity.setProductName(openAIProductPO.getProductName());
        productEntity.setProductDesc(openAIProductPO.getProductDesc());
        productEntity.setQuota(openAIProductPO.getQuota());
        productEntity.setPrice(openAIProductPO.getPrice());
        productEntity.setEnable(OpenAIProductEnableModel.get(openAIProductPO.getIsEnabled()));
        return productEntity;
    }

    @Override
    public void saveOrder(CreateOrderAggregate createOrderAggregate) {
        String openId = createOrderAggregate.getOpenId();
        OrderEntity orderEntity = createOrderAggregate.getOrderEntity();
        ProductEntity productEntity = createOrderAggregate.getProductEntity();

        OpenAIOrderPO openAIOrderPO = new OpenAIOrderPO();
        openAIOrderPO.setOpenid(openId);
        openAIOrderPO.setProductId(productEntity.getProductId());
        openAIOrderPO.setProductName(productEntity.getProductName());
        openAIOrderPO.setProductQuota(productEntity.getQuota());
        openAIOrderPO.setOrderId(orderEntity.getOrderId());
        openAIOrderPO.setOrderTime(orderEntity.getOrderTime());
        openAIOrderPO.setOrderStatus(orderEntity.getOrderStatus().getCode());
        openAIOrderPO.setTotalAmount(orderEntity.getTotalAmount());
        openAIOrderPO.setPayType(orderEntity.getPayTypeVO().getCode());
        openAIOrderPO.setPayUrl(openAIOrderPO.getPayUrl());
        openAIOrderPO.setPayAmount(openAIOrderPO.getPayAmount());
        openAIOrderPO.setPayStatus(PayStatusVO.WAIT.getCode());
//        这里
//        openAIOrderPO.setPayTime();
        openAIOrderDao.insert(openAIOrderPO);


    }

    @Override
    public void updateOrderPayInfo(PayOrderEntity payOrderEntity) {
        OpenAIOrderPO openAIOrderPO = new OpenAIOrderPO();
        openAIOrderPO.setOpenid(payOrderEntity.getOpenid());
        openAIOrderPO.setOrderId(payOrderEntity.getOrderId());
        openAIOrderPO.setPayUrl(payOrderEntity.getPayUrl());
        openAIOrderPO.setPayStatus(payOrderEntity.getPayStatus().getCode());
        openAIOrderDao.updateOrderPayInfo(openAIOrderPO);

    }

    @Override
    public boolean changeOrderPaySuccess(String out_trade_no, String total_fee, String order_no, String success_time) throws ParseException {
        OpenAIOrderPO openAIOrderPO = new OpenAIOrderPO();
        openAIOrderPO.setOrderId(out_trade_no);
        //这里就是存入的decimal类型的字段
        openAIOrderPO.setPayAmount(new BigDecimal(total_fee));
        openAIOrderPO.setTransactionId(order_no);
        /*
        *         String time = "2025-01-19 15:39:51";
        //解析后的时间为:Sun Jan 19 15:39:51 CST 2025
        * 这里存入的就是Date类型的时间
        * */
        openAIOrderPO.setPayTime(format.parse(success_time));
        return openAIOrderDao.changeOrderPaySuccess(openAIOrderPO);


    }
}
