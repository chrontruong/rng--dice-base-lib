package com.io.begstd.dice.utils;

import com.io.begstd.dice.model.config.ICommonDiceConfig;
import com.io.begstd.dice.model.config.DiceConfigMode;
import com.io.begstd.dice.model.config.DiceConfigType;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class ConfigManager {

    @Getter
    private volatile Map<DiceConfigMode, ICommonDiceConfig> mainDiceConfigMap;

    @Autowired
    private List<ICommonDiceConfig> commonDiceConfigs;

    public ICommonDiceConfig getConfigMain(DiceConfigMode mode) {
        return mainDiceConfigMap.get(mode);
    }

    public void updateMainConfig(DiceConfigMode mode, ICommonDiceConfig commonDiceConfig) {
        if (commonDiceConfig != null) {
            mainDiceConfigMap.put(mode, commonDiceConfig);
        }
    }


    @PostConstruct
    private void bindingMainConfig() {
        mainDiceConfigMap = new HashMap<>();

        ICommonDiceConfig baseNormalConfig = null;

        for (ICommonDiceConfig commonDiceConfig : commonDiceConfigs) {
            if (commonDiceConfig.getConfigType() == DiceConfigType.BASE_NORMAL) {
                baseNormalConfig = commonDiceConfig;
            }
        }

        mainDiceConfigMap.put(DiceConfigMode.NORMAL, baseNormalConfig);
    }
}
