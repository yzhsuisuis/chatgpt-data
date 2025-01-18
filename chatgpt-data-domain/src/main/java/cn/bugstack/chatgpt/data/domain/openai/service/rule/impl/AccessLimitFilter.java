package cn.bugstack.chatgpt.data.domain.openai.service.rule.impl;

import cn.bugstack.chatgpt.data.domain.openai.annotation.LogicStrategy;
import cn.bugstack.chatgpt.data.domain.openai.model.aggregates.ChatProcessAggregate;
import cn.bugstack.chatgpt.data.domain.openai.model.entity.RuleLogicEntity;
import cn.bugstack.chatgpt.data.domain.openai.model.entity.UserAccountQuotaEntity;
import cn.bugstack.chatgpt.data.domain.openai.model.valobj.LogicCheckTypeVO;
import cn.bugstack.chatgpt.data.domain.openai.service.rule.ILogicFilter;
import cn.bugstack.chatgpt.data.domain.openai.service.rule.factory.DefaultLogicFactory;
import cn.bugstack.chatgpt.data.types.common.Constants;
import com.google.common.cache.Cache;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
@LogicStrategy(logicMode = DefaultLogicFactory.LogicModel.ACCESS_LIMIT)
public class AccessLimitFilter implements ILogicFilter<UserAccountQuotaEntity> {


    @Value("${app.config.white-list}")
    private String whiteList;

//    原来是自己的这个注解有毛病
    @Value("${app.config.limit-count}")
    private Integer limitCount;

    @Resource
    private Cache<String,Integer> visitCache;



    @Override
    public RuleLogicEntity<ChatProcessAggregate> filter(ChatProcessAggregate chatProcess, UserAccountQuotaEntity userAccountQuotaEntity) throws Exception {
//        白名单过滤掉
        if(chatProcess.isWhiteList(whiteList))
        {
            return RuleLogicEntity.<ChatProcessAggregate>builder()
                    .data(chatProcess)
                    .type(LogicCheckTypeVO.SUCCESS)
                    .info(LogicCheckTypeVO.SUCCESS.getInfo()).build();
        }
//        次数
        Integer hadDoCount = visitCache.get(chatProcess.getOpenid(), () -> 0);
        if(hadDoCount < limitCount)
        {
            visitCache.put(chatProcess.getOpenid(), hadDoCount+1);
            return RuleLogicEntity.<ChatProcessAggregate>builder()
                    .data(chatProcess)
                    .type(LogicCheckTypeVO.SUCCESS)
                    .info(LogicCheckTypeVO.SUCCESS.getInfo()).build();

        }
        return RuleLogicEntity.<ChatProcessAggregate>builder()
                .info("您今日的免费" + limitCount + "次，已耗尽！")
                .type(LogicCheckTypeVO.REFUSE).data(chatProcess).build();


    }
}
