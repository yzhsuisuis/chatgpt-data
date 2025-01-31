package cn.bugstack.chatgpt.data.config;

import cn.bugstack.chatgpt.data.trigger.mq.OrderPaySuccessListener;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.eventbus.EventBus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * @author Fuzhengwei bugstack.cn @小傅哥
 * @description
 * @create 2023-08-05 17:45
 */
@Configuration
public class GoogleGuavaCodeCacheConfig {

    @Bean(name = "codeCache")
    public Cache<String, String> codeCache() {
        return CacheBuilder.newBuilder()
                .expireAfterWrite(3, TimeUnit.MINUTES)
                .build();
    }
//    用户访问频次限制限制
    @Bean(name = "visitCache")
    public Cache<String,Integer> visitCache()
    {
        return CacheBuilder.newBuilder()
                .expireAfterWrite(12,TimeUnit.HOURS)
                .build();
    }
    @Bean
    public EventBus eventBusListener(OrderPaySuccessListener listener){
        EventBus eventBus = new EventBus();
        eventBus.register(listener);
        return eventBus;
    }

}
