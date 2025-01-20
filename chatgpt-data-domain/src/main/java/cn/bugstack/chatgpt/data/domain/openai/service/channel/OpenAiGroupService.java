package cn.bugstack.chatgpt.data.domain.openai.service.channel;

import cn.bugstack.chatgpt.data.domain.openai.model.aggregates.ChatProcessAggregate;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

/**
 * @Author: yangzihe
 * @CreateTime: 2025/01/20  16:16
 * @Description:
 */
public interface OpenAiGroupService {
    //把应答转化成一个接口,来实现一个group
    void doMessageResponse(ChatProcessAggregate chatProcess, ResponseBodyEmitter responseBodyEmitter) throws JsonProcessingException, Exception;
}
