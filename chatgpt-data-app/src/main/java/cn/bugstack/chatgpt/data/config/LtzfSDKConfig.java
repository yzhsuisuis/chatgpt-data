package cn.bugstack.chatgpt.data.config;

import cn.bugstack.ltzf.factory.PayFactory;
import cn.bugstack.ltzf.factory.defaults.DeafultPayFactory;


import cn.bugstack.ltzf.payments.nativepay.NativePayService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@EnableConfigurationProperties(LtzfSDKConfigProperties.class)
public class LtzfSDKConfig {

//    通过一个配置类
    @Bean(name = "payFactory")
    @ConditionalOnProperty(value = "ltzf.sdk.config.enabled", havingValue = "true", matchIfMissing = false)
    public PayFactory payFactory(LtzfSDKConfigProperties properties) {
        cn.bugstack.ltzf.factory.Configuration configuration = new cn.bugstack.ltzf.factory.Configuration(
                properties.getAppId(),
                properties.getMerchantId(),
                properties.getPartnerKey()
        );

//        return new DefaultPayFactory(configuration);
        return new DeafultPayFactory(configuration);
    }


    @Bean(name = "nativePayService")
//    条件注解, 这样的注解方法确实骚气,在其他的类里面以注解的形式来弄
    @ConditionalOnProperty(value = "ltzf.sdk.config.enabled", havingValue = "true", matchIfMissing = false)
    public NativePayService nativePayService(PayFactory payFactory) {
        log.info("蓝兔支付 SDK 启动成功，扫码支付服务已装配");
        return payFactory.nativePayService();
    }

//    @Bean(name = "h5PayService")
//    @ConditionalOnProperty(value = "ltzf.sdk.config.enabled", havingValue = "true", matchIfMissing = false)
//    public H5PayService h5PayService(PayFactory payFactory) {
//        log.info("蓝兔支付 SDK 启动成功，H5支付服务已装配");
//        return payFactory.h5PayService();
//    }

}
