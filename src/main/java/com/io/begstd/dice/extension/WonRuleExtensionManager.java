package com.io.begstd.dice.extension;

import com.io.begstd.extension.Extension;
import com.io.begstd.dice.exception.ExtensionException;
import com.io.begstd.dice.factory.GameRuleFactory;
import com.io.begstd.dice.machine.wonrule.WonRuleExtension;
import com.io.begstd.dice.model.config.IDiceMachineConfig;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WonRuleExtensionManager implements Extension {
    //Extension Rule will be load to this map
    Map<String, Extension> wonRules = new HashMap<>();

    Map<Integer, List<WonRuleExtension>> rulesMap = new HashMap<>();

    public WonRuleExtensionManager clone() {
        WonRuleExtensionManager wonRuleExtensionManager = new WonRuleExtensionManager();
        wonRuleExtensionManager.wonRules.putAll(this.wonRules);
        return wonRuleExtensionManager;
    }

    public List<WonRuleExtension> getWonRules(IDiceMachineConfig slotMachineConfig, int mode) {
        if(!rulesMap.containsKey(mode)) {
            synchronized (this) {
                if(!rulesMap.containsKey(mode)) {
                    List<WonRuleExtension> rules = new ArrayList<>();
                    updateWonRuleList(slotMachineConfig.wonRules(), rules);
                    rulesMap.put(mode, rules);
                }
            }
        }
        return rulesMap.get(mode);
    }
    
    public List<WonRuleExtension> getWonRules(List<WonRuleExtension> ruleList, int mode) {
        if(!rulesMap.containsKey(mode)) {
            synchronized (this) {
                if(!rulesMap.containsKey(mode)) {
                    List<WonRuleExtension> rules = new ArrayList<>();
                    updateWonRuleList(ruleList, rules);
                    rulesMap.put(mode, rules);
                }
            }
        }
        return rulesMap.get(mode);
    }

    private void updateWonRuleList(List<WonRuleExtension> configRules, List<WonRuleExtension> processingRules) {
        for (WonRuleExtension gameAmountWonRule : configRules) {
            if (wonRules.containsKey(gameAmountWonRule.getName())) {
                processingRules.add((WonRuleExtension) wonRules.get(gameAmountWonRule.getName()));
            } else {
                processingRules.add(gameAmountWonRule);
            }
        }
    }

    public void addChild(Extension extension) {
        wonRules.put(extension.getName(), extension);
    }

    public void verify(List<IDiceMachineConfig> iDiceMachineConfigs, GameRuleFactory gameRuleFactory) {
        for (IDiceMachineConfig iDiceMachineConfig : iDiceMachineConfigs) {
            for (String ruleName : iDiceMachineConfig.wonRulesName()) {
                if(!wonRules.containsKey(ruleName) && !gameRuleFactory.containsRule(ruleName)) {
                    throw new ExtensionException("WonRules not valid");
                }
            }
        }
    }

    @Override
    public String getName() {
        return WonRuleExtensionManager.class.getSimpleName();
    }
}
