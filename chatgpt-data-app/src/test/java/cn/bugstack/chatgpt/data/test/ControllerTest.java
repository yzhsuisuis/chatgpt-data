package cn.bugstack.chatgpt.data.test;

import cn.bugstack.chatgpt.data.trigger.http.ChatGPTAIServiceController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
public class ControllerTest {

    @Autowired
    private ApplicationContext applicationContext;

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
}