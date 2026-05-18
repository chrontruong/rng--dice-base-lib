package com.io.begstd.slot.utils;

import com.io.begstd.slot.model.config.ICommonSlotConfig;
import com.io.begstd.slot.model.config.SlotConfigMode;
import com.io.begstd.slot.model.config.SlotConfigType;
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
    private volatile Map<SlotConfigMode, ICommonSlotConfig> mainSlotConfigMap;

    @Autowired
    private List<ICommonSlotConfig> commonSlotConfigs;

    public ICommonSlotConfig getConfigMain(SlotConfigMode mode) {
        return mainSlotConfigMap.get(mode);
    }

    public void updateMainConfig(SlotConfigMode mode, ICommonSlotConfig commonSlotConfig) {
        if (commonSlotConfig != null) {
            mainSlotConfigMap.put(mode, commonSlotConfig);
        }
    }


    @PostConstruct
    private void bindingMainConfig() {
        mainSlotConfigMap = new HashMap<>();

        ICommonSlotConfig baseNormalConfig = null;
        ICommonSlotConfig baseFreeConfig = null;
        ICommonSlotConfig baseMiniConfig = null;
        ICommonSlotConfig baseGambleConfig = null;

        ICommonSlotConfig customNormalConfig = null;
        ICommonSlotConfig customFreeConfig = null;
        ICommonSlotConfig customMiniConfig = null;
        ICommonSlotConfig customGambleConfig = null;

        for (ICommonSlotConfig commonSlotConfig : commonSlotConfigs) {
            if (commonSlotConfig.getConfigType() == SlotConfigType.BASE_NORMAL) {
                baseNormalConfig = commonSlotConfig;
            } else if (commonSlotConfig.getConfigType() == SlotConfigType.BASE_FREE) {
                baseFreeConfig = commonSlotConfig;
            } else if (commonSlotConfig.getConfigType() == SlotConfigType.BASE_MINI) {
                baseMiniConfig = commonSlotConfig;
            } else if (commonSlotConfig.getConfigType() == SlotConfigType.BASE_GAMBLE) {
                baseGambleConfig = commonSlotConfig;
            } else if (commonSlotConfig.getConfigType() == SlotConfigType.CUSTOM_NORMAL) {
                customNormalConfig = commonSlotConfig;
            } else if (commonSlotConfig.getConfigType() == SlotConfigType.CUSTOM_FREE) {
                customFreeConfig = commonSlotConfig;
            } else if (commonSlotConfig.getConfigType() == SlotConfigType.CUSTOM_MINI) {
                customMiniConfig = commonSlotConfig;
            } else if (commonSlotConfig.getConfigType() == SlotConfigType.CUSTOM_GAMBLE) {
                customGambleConfig = commonSlotConfig;
            }
        }

        mainSlotConfigMap.put(SlotConfigMode.NORMAL,
                customNormalConfig != null ? customNormalConfig : baseNormalConfig);
        mainSlotConfigMap.put(SlotConfigMode.FREE,
                customFreeConfig != null ? customFreeConfig : baseFreeConfig);
        mainSlotConfigMap.put(SlotConfigMode.MINI,
                customMiniConfig != null ? customMiniConfig : baseMiniConfig);
        mainSlotConfigMap.put(SlotConfigMode.GAMBLE,
                customGambleConfig != null ? customGambleConfig : baseGambleConfig);
    }
}
