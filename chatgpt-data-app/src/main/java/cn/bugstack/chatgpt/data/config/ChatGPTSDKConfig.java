package cn.bugstack.chatgpt.data.config;

import cn.bugstack.chatgpt.session.OpenAiSession;
import cn.bugstack.chatgpt.session.defaults.DefaultOpenAiSessionFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(ChatGPTSDKConfigProperties.class)
public class ChatGPTSDKConfig {
    @Bean
    public OpenAiSession openAiSession(ChatGPTSDKConfigProperties properties)
    {
        cn.bugstack.chatgpt.session.Configuration configuration = new cn.bugstack.chatgpt.session.Configuration();
        configuration.setApiKey(properties.getApiKey());
        configuration.setApiHost(properties.getApiHost());
        configuration.setAuthToken(properties.getAuthToken());
        DefaultOpenAiSessionFactory factory = new DefaultOpenAiSessionFactory(configuration);
        return factory.openSession();

    }
}
