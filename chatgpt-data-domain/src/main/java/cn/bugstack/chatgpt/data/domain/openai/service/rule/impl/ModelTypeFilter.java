package cn.bugstack.chatgpt.data.domain.openai.service.rule.impl;

import cn.bugstack.chatgpt.data.domain.openai.annotation.LogicStrategy;
import cn.bugstack.chatgpt.data.domain.openai.model.aggregates.ChatProcessAggregate;
import cn.bugstack.chatgpt.data.domain.openai.model.entity.RuleLogicEntity;
import cn.bugstack.chatgpt.data.domain.openai.model.entity.UserAccountQuotaEntity;
import cn.bugstack.chatgpt.data.domain.openai.model.valobj.LogicCheckTypeVO;
import cn.bugstack.chatgpt.data.domain.openai.service.rule.ILogicFilter;
import cn.bugstack.chatgpt.data.domain.openai.service.rule.factory.DefaultLogicFactory;
import org.springframework.stereotype.Component;

import java.util.List;

//判断用户模型是否可用
@Component
@LogicStrategy(logicMode = DefaultLogicFactory.LogicModel.MODEL_TYPE)
public class ModelTypeFilter implements ILogicFilter<UserAccountQuotaEntity> {
    @Override
    public RuleLogicEntity<ChatProcessAggregate> filter(ChatProcessAggregate chatProcess, UserAccountQuotaEntity userAccountQuotaEntity) throws Exception {
        List<String> allowModelTypeList = userAccountQuotaEntity.getAllowModelTypeList();
        String modelType = chatProcess.getModel();
        if (allowModelTypeList.contains(modelType)) {
//            在允许的范围内,可以放行
            return RuleLogicEntity.<ChatProcessAggregate>builder()
                    .type(LogicCheckTypeVO.SUCCESS)
                    .data(chatProcess).build();

        }

        return RuleLogicEntity.<ChatProcessAggregate>builder()
                .type(LogicCheckTypeVO.REFUSE).info("当前账户不支持使用 " + modelType + " 模型！可以联系客服升级账户。")
                .data(chatProcess).build();


    }
}
