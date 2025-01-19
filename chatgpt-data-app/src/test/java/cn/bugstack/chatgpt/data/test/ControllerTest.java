package cn.bugstack.chatgpt.data.test;

import cn.bugstack.chatgpt.data.domain.openai.model.entity.UserAccountQuotaEntity;
import cn.bugstack.chatgpt.data.domain.openai.repository.IOpenAiRepository;
import cn.bugstack.chatgpt.data.trigger.http.ChatGPTAIServiceController;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import javax.annotation.Resource;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import static org.junit.jupiter.api.Assertions.assertNotNull;
@Slf4j
@SpringBootTest
public class ControllerTest {
    @Resource
    private IOpenAiRepository openAiRepository;

    @Autowired
    private ApplicationContext applicationContext;

    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Test
    public void testControllerRegistration() {
        try {
            ChatGPTAIServiceController controller = applicationContext.getBean(ChatGPTAIServiceController.class);
            assertNotNull(controller);
            System.out.println("ChatGPTAIServiceController is registered!");
        } catch (BeansException e) {
            System.out.println("ChatGPTAIServiceController is NOT registered!");
        }
    }

    @Test
    public void test() throws ParseException {
        BigDecimal bigDecimal = new BigDecimal("0.01");
        System.out.println(bigDecimal.toString());
        log.info("decimal:{}",bigDecimal.toString());
        String time = "2025-01-19 15:39:51";
        //解析后的时间为:Sun Jan 19 15:39:51 CST 2025
        log.info("解析后的时间为:{}",format.parse(time));



    }
}