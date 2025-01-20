package cn.bugstack.chatgpt.data.infrastructure.repository;

import cn.bugstack.chatgpt.data.domain.auth.service.IAuthService;
import cn.bugstack.chatgpt.data.domain.order.model.aggregates.CreateOrderAggregate;
import cn.bugstack.chatgpt.data.domain.order.model.entity.*;
import cn.bugstack.chatgpt.data.domain.order.model.valobj.PayStatusVO;
import cn.bugstack.chatgpt.data.domain.order.repository.IOrderRepository;
import cn.bugstack.chatgpt.data.infrastructure.dao.IOpenAIOrderDao;
import cn.bugstack.chatgpt.data.infrastructure.dao.IOpenAIProductDao;
import cn.bugstack.chatgpt.data.infrastructure.dao.IUserAccountDao;
import cn.bugstack.chatgpt.data.infrastructure.po.OpenAIOrderPO;
import cn.bugstack.chatgpt.data.infrastructure.po.OpenAIProductPO;
import cn.bugstack.chatgpt.data.infrastructure.po.UserAccountPO;
import cn.bugstack.chatgpt.data.types.enums.OpenAIProductEnableModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
@Slf4j
@Repository
public class OrderRepository implements IOrderRepository {
    @Resource
    private IOpenAIOrderDao openAIOrderDao;

    @Resource
    private IOpenAIProductDao openAIProductDao;

    @Resource
    private IUserAccountDao userAccountDao;

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
        log.info("total_fee转化成new BigDecimal:{}",new BigDecimal(total_fee));
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

    @Override
    public void deliverGoods(String orderId) {
        OpenAIOrderPO openAIOrderPO = openAIOrderDao.queryOrder(orderId);
        // 1. 变更发货状态
        int updateOrderStatusDeliverGoodsCount = openAIOrderDao.updateOrderStatusDeliverGoods(orderId);
        if (1 != updateOrderStatusDeliverGoodsCount) throw new RuntimeException("updateOrderStatusDeliverGoodsCount update count is not equal 1");
        // 2. 账户额度变更
        UserAccountPO userAccountPO = userAccountDao.queryUserAccount(openAIOrderPO.getOpenid());
        UserAccountPO userAccountPOReq = new UserAccountPO();
        userAccountPOReq.setOpenid(openAIOrderPO.getOpenid());
        userAccountPOReq.setTotalQuota(openAIOrderPO.getProductQuota());
        userAccountPOReq.setSurplusQuota(openAIOrderPO.getProductQuota());
        if (null != userAccountPO){
            int addAccountQuotaCount = userAccountDao.addAccountQuota(userAccountPOReq);
            if (1 != addAccountQuotaCount) throw new RuntimeException("addAccountQuotaCount update count is not equal 1");
        } else {
            userAccountDao.insert(userAccountPOReq);
        }
    }

    @Override
    public List<String> queryTimeoutCloseOrderList() {
        return openAIOrderDao.queryTimeoutCloseOrderList();

    }

    @Override
    public boolean changeOrderClose(String orderId) {
        return openAIOrderDao.changeOrderClose(orderId);
    }

    @Override
    public List<String> queryNoPayNotifyOrder() {
        return openAIOrderDao.queryNoPayNotifyOrder();
    }

    @Override
    public List<String> queryReplenishmentOrder() {
        return openAIOrderDao.queryReplenishmentOrder();
    }

    @Override
    public List<ProductEntity> queryProductList() {
        List<OpenAIProductPO> openAIProductPOList =  openAIProductDao.queryProductList();
        List<ProductEntity> productEntityList = new ArrayList<>(openAIProductPOList.size());
        for (OpenAIProductPO openAIProductPO : openAIProductPOList) {
            ProductEntity productEntity = new ProductEntity();
            productEntity.setProductId(openAIProductPO.getProductId());
            productEntity.setProductName(openAIProductPO.getProductName());
            productEntity.setProductDesc(openAIProductPO.getProductDesc());
            productEntity.setQuota(openAIProductPO.getQuota());
            productEntity.setPrice(openAIProductPO.getPrice());
            productEntityList.add(productEntity);
        }
        return productEntityList;
    }

    @Override
    public String queryUserAccountQuota(String openid) {
        UserAccountPO userAccountPO = userAccountDao.queryUserAccount(openid);
        Integer surplusQuota = userAccountPO.getSurplusQuota();
        return surplusQuota.toString();
    }
}
