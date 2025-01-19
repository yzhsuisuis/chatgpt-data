package cn.bugstack.chatgpt.data.domain.order.model.aggregates;

import cn.bugstack.chatgpt.data.domain.order.model.entity.OrderEntity;
import cn.bugstack.chatgpt.data.domain.order.model.entity.ProductEntity;
import cn.hutool.db.sql.Order;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateOrderAggregate{

    private String openId;

    private OrderEntity orderEntity;

    private ProductEntity productEntity;
}
