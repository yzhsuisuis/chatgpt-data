package cn.bugstack.chatgpt.data.domain.openai.service;

import cn.bugstack.chatgpt.data.domain.openai.model.aggregates.ChatProcessAggregate;
import cn.bugstack.chatgpt.data.domain.openai.model.entity.RuleLogicEntity;
import cn.bugstack.chatgpt.data.domain.openai.model.entity.UserAccountQuotaEntity;
import cn.bugstack.chatgpt.data.domain.openai.model.valobj.LogicCheckTypeVO;
import cn.bugstack.chatgpt.data.domain.openai.repository.IOpenAiRepository;
import cn.bugstack.chatgpt.data.domain.openai.service.rule.factory.DefaultLogicFactory;
import cn.bugstack.chatgpt.data.types.common.Constants;
import cn.bugstack.chatgpt.data.types.exception.ChatGPTException;
import cn.bugstack.chatgpt.session.OpenAiSession;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

import javax.annotation.Resource;
@Slf4j
public abstract class AbstractChatService implements IChatService{
    @Resource
    protected OpenAiSession openAiSession;

    @Resource
    private IOpenAiRepository openAiRepository;


    @Override
    public ResponseBodyEmitter completions(ResponseBodyEmitter emitter, ChatProcessAggregate chatProcess) {


        // 2. 应答处理
        try {
            // 1. 请求应答
            emitter.onCompletion(() -> {
                log.info("流式问答请求完成，使用模型：{}", chatProcess.getModel());
            });
            emitter.onError(throwable -> log.error("流式问答请求疫情，使用模型：{}", chatProcess.getModel(), throwable));


// 2. 规则过滤
//            UserAccountQuotaEntity userAccountQuotaEntity = new UserAccountQuotaEntity();
            UserAccountQuotaEntity userAccountQuotaEntity = openAiRepository.queryUserAccount(chatProcess.getOpenid());


            RuleLogicEntity<ChatProcessAggregate> ruleLogicEntity = this.doCheckLogic(chatProcess,userAccountQuotaEntity,
                    DefaultLogicFactory.LogicModel.ACCESS_LIMIT.getCode(),
                    DefaultLogicFactory.LogicModel.SENSITIVE_WORD.getCode(),
                    DefaultLogicFactory.LogicModel.ACCOUNT_STATUS.getCode(),
                    DefaultLogicFactory.LogicModel.MODEL_TYPE.getCode(),
                    DefaultLogicFactory.LogicModel.USER_QUOTA.getCode());

            if (!LogicCheckTypeVO.SUCCESS.equals(ruleLogicEntity.getType())) {
//                如果过滤链没成功,则放行
                emitter.send(ruleLogicEntity.getInfo());
                emitter.complete();
                return emitter;
            }

            log.info("校验后的结果:{}",ruleLogicEntity.getData().getMessages());

            this.doMessageResponse(chatProcess, emitter);
        } catch (Exception e) {
            throw new ChatGPTException(Constants.ResponseCode.UN_ERROR.getCode(), Constants.ResponseCode.UN_ERROR.getInfo());
        }

        // 3. 返回结果
        return emitter;
    }

    protected abstract void doMessageResponse(ChatProcessAggregate chatProcess, ResponseBodyEmitter responseBodyEmitter) throws JsonProcessingException;
    protected abstract RuleLogicEntity<ChatProcessAggregate> doCheckLogic(ChatProcessAggregate chatProcess, UserAccountQuotaEntity userAccountQuotaEntity, String ...logicfilters) throws Exception;

}
