package cn.bugstack.chatgpt.data.domain.openai.service;

import cn.bugstack.chatgpt.common.Constants;
import cn.bugstack.chatgpt.data.domain.openai.model.aggregates.ChatProcessAggregate;

import cn.bugstack.chatgpt.data.domain.openai.model.entity.RuleLogicEntity;
import cn.bugstack.chatgpt.data.domain.openai.model.entity.UserAccountQuotaEntity;
import cn.bugstack.chatgpt.data.domain.openai.model.valobj.LogicCheckTypeVO;
import cn.bugstack.chatgpt.data.domain.openai.service.rule.ILogicFilter;
import cn.bugstack.chatgpt.data.domain.openai.service.rule.factory.DefaultLogicFactory;
import cn.bugstack.chatgpt.domain.chat.ChatChoice;
import cn.bugstack.chatgpt.domain.chat.ChatCompletionRequest;
import cn.bugstack.chatgpt.domain.chat.ChatCompletionResponse;
import cn.bugstack.chatgpt.domain.chat.Message;
import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import okhttp3.sse.EventSource;
import okhttp3.sse.EventSourceListener;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
@Slf4j
@Service
public class ChatService extends AbstractChatService{

    @Resource
    private DefaultLogicFactory logicFactory;

    @Override
    protected void doMessageResponse(ChatProcessAggregate chatProcess, ResponseBodyEmitter emitter) throws JsonProcessingException {
//是在这一个类里面,但是你的方法不对
        List<Message> messages = chatProcess.getMessages().stream()
                .map(entity -> Message.builder()
                                .content(entity.getContent())
                                .name(entity.getName())
//                        这里相当于对这个进行一个约束
                                .role(Constants.Role.valueOf(entity.getRole().toUpperCase()))
                                .build()

                ).collect(Collectors.toList());
        ChatCompletionRequest chatCompletion = ChatCompletionRequest
                .builder()
                .stream(true)
                .messages(messages)
                .model(chatProcess.getModel())
                .build();
//        openAiSession.completions(chatCompletion,
//                new EventSourceListener() {
//                    @Override
//                    public void onEvent(@NotNull EventSource eventSource, @Nullable String id, @Nullable String type, @NotNull String data) {
////                相同对话的返回的相应块的id是相同的
//
//                    }
//                });
        openAiSession.chatCompletions(chatCompletion, new EventSourceListener() {
            @Override
            public void onEvent(@NotNull EventSource eventSource, @Nullable String id, @Nullable String type, @NotNull String data) {
                ChatCompletionResponse chatCompletionResponse = JSON.parseObject(data, ChatCompletionResponse.class);
                                    /*
                    {
  "id": "chatcmpl-123",
  "object": "chat.completion.chunk",
  "created": 1694268190,
  "model": "gpt-4o-mini",
  "system_fingerprint": "fp_44709d6fcb",
  "choices": [
    {
      "index": 0,
      "delta": {
        "role": "assistant",
        "content": ""
      },
      "logprobs": null,
      "finish_reason": null
    }
  ]
}
                    *
                    *
{"id":"chatcmpl-123","object":"chat.completion.chunk","created":1694268190,"model":"gpt-4o-mini", "system_fingerprint": "fp_44709d6fcb", "choices":[{"index":0,"delta":{"role":"assistant","content":""},"logprobs":null,"finish_reason":null}]}
{"id":"chatcmpl-123","object":"chat.completion.chunk","created":1694268190,"model":"gpt-4o-mini", "system_fingerprint": "fp_44709d6fcb", "choices":[{"index":0,"delta":{"content":"Hello"},"logprobs":null,"finish_reason":null}]}
{"id":"chatcmpl-123","object":"chat.completion.chunk","created":1694268190,"model":"gpt-4o-mini", "system_fingerprint": "fp_44709d6fcb", "choices":[{"index":0,"delta":{},"logprobs":null,"finish_reason":"stop"}]}

                    * */
                List<ChatChoice> choices = chatCompletionResponse.getChoices();
                for (ChatChoice choice : choices) {
                    Message delta = choice.getDelta();
//                    根据他的语句来判断这个是该怎么个结束法
                    if(Constants.Role.ASSISTANT.getCode().equals(delta.getRole())) continue;

                    String finishReason = choice.getFinishReason();

                    if (StringUtils.isNoneBlank(finishReason) && "stop".equals(finishReason)) {
                        emitter.complete();
                        break;
                    }

                    // 发送信息
                    try {
                        emitter.send(delta.getContent());
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }


                }
//

            }
        });


    }

    @Override
    protected RuleLogicEntity<ChatProcessAggregate> doCheckLogic(ChatProcessAggregate chatProcess, UserAccountQuotaEntity userAccountQuotaEntity , String... logicfilters) throws Exception {
        RuleLogicEntity<ChatProcessAggregate> ruleLogicEntity = null;
        Map<String, ILogicFilter> logicFilterMap = logicFactory.openLogicFilter();
        for (String logicfilter : logicfilters) {
//            能在map里找到
//            在后面的用户的某些过滤的情况,需要使用到用户的信息,所以在这个时候,需要在外面先根据openid查询得到用户的信息,然后再传入进来
//            而openid是根据用的token,然后使用authService.getopenid
            ruleLogicEntity = logicFilterMap.get(logicfilter).filter(chatProcess,userAccountQuotaEntity);
            log.info("过滤链是:{}",logicfilter);
            if(!LogicCheckTypeVO.SUCCESS.equals(ruleLogicEntity.getType()))
            {
//                被拦截了
                return ruleLogicEntity;
            }
        }
        return ruleLogicEntity!=null ? ruleLogicEntity: RuleLogicEntity.<ChatProcessAggregate>builder()
                .type(LogicCheckTypeVO.SUCCESS)
                .data(chatProcess).build();
//        有可能根本就没有logicFilter的设置所以就直接



    }
}
