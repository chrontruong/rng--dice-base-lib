package com.io.begstd.slot.factory;

import com.io.begstd.slot.machine.wonrule.WonRuleExtension;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class GameRuleFactory implements InitializingBean {

    private Map<String, WonRuleExtension> wonRuleMap;

    public GameRuleFactory(List<WonRuleExtension> wonRuleMap) {
        log.info(wonRuleMap.toString());
        initWonRule(wonRuleMap);
    }

    private void initWonRule(List<WonRuleExtension> wonRules) {
        wonRuleMap = new HashMap<>();
        wonRules.forEach(wonRule -> {
            wonRuleMap.put(wonRule.getName(), wonRule);
        });
    }

    public List<WonRuleExtension> getGameWonRules(List<String> ruleTypes) {
        List<WonRuleExtension> ruleList = new ArrayList<>();
        for (String ruleType : ruleTypes) {
            ruleList.add(wonRuleMap.get(ruleType));
        }
        return ruleList;
    }

    public boolean containsRule(String ruleName) {
        return wonRuleMap.containsKey(ruleName);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (wonRuleMap == null || wonRuleMap.isEmpty()) {
            throw new RuntimeException("Initial dependency from GameFuleFactory fail for gameWonRules.");
        }
    }
}
