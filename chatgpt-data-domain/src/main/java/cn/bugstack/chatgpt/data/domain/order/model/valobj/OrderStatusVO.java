package cn.bugstack.chatgpt.data.domain.order.model.valobj;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author Fuzhengwei bugstack.cn @小傅哥
 * @description 订单状态
 * @create 2023-10-05 07:20
 */
@Getter
@AllArgsConstructor
public enum OrderStatusVO {

    CREATE(0, "创建完成"),
    WAIT(1,"等待发货"),
    COMPLETED(2,"发货完成"),
    CLOSE(3,"系统关单"),
    ;

    private final Integer code;
    private final String desc;

    public static OrderStatusVO get(Integer code){
        switch (code){
            case 0:
                return OrderStatusVO.CREATE;
            case 1:
                return OrderStatusVO.WAIT;
            case 2:
                return OrderStatusVO.COMPLETED;
            case 3:
                return OrderStatusVO.CLOSE;
            default:
                return OrderStatusVO.CREATE;
        }
    }

}
