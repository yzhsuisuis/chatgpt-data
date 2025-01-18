package cn.bugstack.chatgpt.data.test;

import cn.bugstack.chatgpt.data.domain.openai.model.entity.UserAccountQuotaEntity;
import cn.bugstack.chatgpt.data.domain.openai.repository.IOpenAiRepository;
import cn.bugstack.chatgpt.data.trigger.http.ChatGPTAIServiceController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import javax.annotation.Resource;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
public class ControllerTest {
    @Resource
    private IOpenAiRepository openAiRepository;

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

    @Test
    public void test()
    {
        UserAccountQuotaEntity userAccountQuotaEntity = openAiRepository.queryUserAccount("gh_c5ce6e4a0e0e");
        System.out.println(userAccountQuotaEntity);

    }
}