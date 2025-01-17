package cn.bugstack.chatgpt.data.domain.openai.annotation;

import cn.bugstack.chatgpt.data.domain.openai.service.rule.factory.DefaultLogicFactory;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface LogicStrategy {
//    这里有点像方法,但是其实是注解的属性

    DefaultLogicFactory.LogicModel logicMode();

}
