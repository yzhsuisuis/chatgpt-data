package cn.bugstack.chatgpt.data.domain.openai.model.entity;

import cn.bugstack.chatgpt.data.domain.openai.model.valobj.UserAccountStatusVO;
import cn.bugstack.chatgpt.data.types.common.Constants;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.List;

/**
 * @author Fuzhengwei bugstack.cn @小傅哥
 * @description 用户账户额度实体对象
 * @create 2023-10-03 16:49
 */
//更加佐证了之前的结论,数据库里存入的东西和实际在控制层要使用的东西,差别其实很大的,所以这里需要,再配置一个中间过渡的类
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserAccountQuotaEntity {

    /**
     * 用户ID
     */
    private String openid;
    /**
     * 总量额度
     */
    private Integer totalQuota;
    /**
     * 剩余额度
     */
    private Integer surplusQuota;
    /**
     * 账户状态,available和freeze的状态
     */
    private UserAccountStatusVO userAccountStatusVO;
    /**
     * 模型类型；一个卡支持多个模型调用，这代表了允许使用的模型范围
     */
    private List<String> allowModelTypeList;

    public void genModelTypes(String modelTypes) {
        String[] vals = modelTypes.split(Constants.SPLIT);
        this.allowModelTypeList = Arrays.asList(vals);
    }

}
