package cn.bugstack.chatgpt.data.trigger.http;

import cn.bugstack.chatgpt.common.Constants;
import cn.bugstack.chatgpt.data.domain.openai.model.aggregates.ChatProcessAggregate;
import cn.bugstack.chatgpt.data.domain.openai.model.entity.MessageEntity;
import cn.bugstack.chatgpt.data.domain.openai.service.IChatService;
import cn.bugstack.chatgpt.data.trigger.http.dto.ChatGPTRequestDTO;
import cn.bugstack.chatgpt.data.types.exception.ChatGPTException;
import cn.bugstack.chatgpt.domain.chat.Message;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Slf4j
@RestController()
@CrossOrigin("${app.config.cross-origin}")
@RequestMapping("/api/${app.config.api-version}/chatgpt/")
public class ChatGPTAIServiceController {

    @Resource
    private IChatService chatService;
    public ChatGPTAIServiceController()
    {
        log.info("我被创建了");
        System.out.println("MyController is being initialized by Spring!");
    }


/*
* 对异步流式响应方法的实验
*
* */
    @RequestMapping(value = "/bodyEmitter", method = RequestMethod.GET)
    public ResponseBodyEmitter handle() {
        // 创建一个ResponseBodyEmitter，-1代表不超时
        ResponseBodyEmitter emitter = new ResponseBodyEmitter(-1L);

        // 异步执行耗时操作
        CompletableFuture.runAsync(() -> {
            try {
                for (int i = 0; i < 10000; i++) {
                    System.out.println("bodyEmitter " + i);
                    // 发送数据
                    emitter.send("bodyEmitter " + i + " @ " + new Date() + "\n");
                    Thread.sleep(2000);
                }
                // 完成
                emitter.complete();
            } catch (Exception e) {
                // 发生异常时结束接口
                emitter.completeWithError(e);
            }
        });

        return emitter;
    }
    @RequestMapping(value = "/chat/completions", method = RequestMethod.POST)
    public ResponseBodyEmitter completionsStream(@RequestBody ChatGPTRequestDTO request, @RequestHeader("Authorization") String token, HttpServletResponse response) {
        log.info("流式问答请求开始，使用模型：{} 请求信息：{}", request.getModel(), JSON.toJSONString(request.getMessages()));
        try {
//        设置相应的编码格式
            // 1. 基础配置；流式输出、编码、禁用缓存
            response.setContentType("text/event-stream");
            response.setCharacterEncoding("UTF-8");
            response.setHeader("Cache-Control", "no-cache");

            // 2. 构建参数
            ChatProcessAggregate chatProcessAggregate = ChatProcessAggregate.builder()
                    .token(token)
                    .model(request.getModel())
                    .messages(request.getMessages().stream()
                            .map(entity -> MessageEntity.builder()
                                    .role(entity.getRole())
                                    .content(entity.getContent())
                                    .name(entity.getName())
                                    .build())
                            .collect(Collectors.toList()))
                    .build();
            return chatService.completions(chatProcessAggregate);
        } catch (Exception e) {
            log.error("流式应答，请求模型：{} 发生异常", request.getModel(), e);
            throw new ChatGPTException(e.getMessage());
        }
    }



}