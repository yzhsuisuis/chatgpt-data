package cn.bugstack.chatgpt.data.domain.openai.service.rule.impl;

import cn.bugstack.chatgpt.data.domain.openai.annotation.LogicStrategy;
import cn.bugstack.chatgpt.data.domain.openai.model.aggregates.ChatProcessAggregate;
import cn.bugstack.chatgpt.data.domain.openai.model.entity.RuleLogicEntity;
import cn.bugstack.chatgpt.data.domain.openai.model.entity.UserAccountQuotaEntity;
import cn.bugstack.chatgpt.data.domain.openai.model.valobj.LogicCheckTypeVO;
import cn.bugstack.chatgpt.data.domain.openai.repository.IOpenAiRepository;
import cn.bugstack.chatgpt.data.domain.openai.service.rule.ILogicFilter;
import cn.bugstack.chatgpt.data.domain.openai.service.rule.factory.DefaultLogicFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author Fuzhengwei bugstack.cn @小傅哥
 * @description 用户额度扣减规则过滤
 * @create 2023-10-03 16:48
 */
@Slf4j
@Component
@LogicStrategy(logicMode = DefaultLogicFactory.LogicModel.USER_QUOTA)
public class UserQuotaFilter implements ILogicFilter<UserAccountQuotaEntity> {

    @Resource
    private IOpenAiRepository openAiRepository;

    @Override
    public RuleLogicEntity<ChatProcessAggregate> filter(ChatProcessAggregate chatProcess, UserAccountQuotaEntity data) throws Exception {
        if (data.getSurplusQuota() > 0) {

//          果然这里和大营销一样也是在过滤链里面进行扣减
            int updateCount = openAiRepository.subAccountQuota(data.getOpenid());
            if (0 != updateCount) {
                return RuleLogicEntity.<ChatProcessAggregate>builder()
                        .type(LogicCheckTypeVO.SUCCESS).data(chatProcess).build();
            }

            return RuleLogicEntity.<ChatProcessAggregate>builder()
                    .info("个人账户，总额度【" + data.getTotalQuota() + "】次，已耗尽！")
                    .type(LogicCheckTypeVO.REFUSE).data(chatProcess).build();
        }

        return RuleLogicEntity.<ChatProcessAggregate>builder()
                .info("个人账户，总额度【" + data.getTotalQuota() + "】次，已耗尽！")
                .type(LogicCheckTypeVO.REFUSE).data(chatProcess).build();
    }

}
