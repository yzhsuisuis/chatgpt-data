package cn.bugstack.chatgpt.data.domain.openai.service.rule.factory;

import cn.bugstack.chatgpt.data.domain.openai.annotation.LogicStrategy;
import cn.bugstack.chatgpt.data.domain.openai.service.rule.ILogicFilter;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class DefaultLogicFactory {
    public Map<String, ILogicFilter> logicFilterMap = new ConcurrentHashMap<>();

    public DefaultLogicFactory(List<ILogicFilter> logicFilters)
    {
        logicFilters.forEach( logic -> {
            LogicStrategy strategy = logic.getClass().getAnnotation(LogicStrategy.class);
            if(null != strategy)
            {
                logicFilterMap.put(strategy.logicMode().getCode(),logic);
            }
        });

    }
//    工厂对外提供的唯一方法就应该是提供构造方法产生的map集合
    public Map<String,ILogicFilter> openLogicFilter()
    {
        return logicFilterMap;
    }


    public enum LogicModel {

        NULL("NULL", "放行不用过滤"),
        ACCESS_LIMIT("ACCESS_LIMIT", "访问次数过滤"),
        SENSITIVE_WORD("SENSITIVE_WORD", "敏感词过滤"),
        USER_QUOTA("USER_QUOTA", "用户额度过滤"),
        MODEL_TYPE("MODEL_TYPE", "模型可用范围过滤"),
        ACCOUNT_STATUS("ACCOUNT_STATUS", "账户状态过滤");

        private String code;
        private String info;

        LogicModel(String code, String info) {
            this.code = code;
            this.info = info;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getInfo() {
            return info;
        }

        public void setInfo(String info) {
            this.info = info;
        }
    }
}
