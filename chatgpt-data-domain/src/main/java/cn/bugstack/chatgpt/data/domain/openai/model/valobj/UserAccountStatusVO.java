package cn.bugstack.chatgpt.data.domain.openai.model.valobj;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
//又差点忘记了枚举类里面不能使用@DATA

@Getter
@AllArgsConstructor
public enum UserAccountStatusVO {

    AVAILABLE(0, "可用"),
    FREEZE(1,"冻结"),
    ;

    private final Integer code;
    private final String info;
// 枚举类里面也是可以使用

    public static UserAccountStatusVO get(Integer code){
        switch (code){
            case 0:
                return UserAccountStatusVO.AVAILABLE;
            case 1:
                return UserAccountStatusVO.FREEZE;
            default:
                return UserAccountStatusVO.AVAILABLE;
        }
    }

}