package com.io.begstd.slot.services.extension.gameplay.impl;

import com.io.begstd.log.LogMessage.LogMessageBuilder;
import com.io.begstd.slot.model.app.BasePlaySession;
import com.io.begstd.slot.model.app.UserInfo;
import com.io.begstd.slot.model.config.ICommonSlotConfig;
import com.io.begstd.slot.model.config.SlotConfigMode;
import com.io.begstd.slot.services.extension.gameplay.LightningGamePlayExtension;

import java.util.Map;

public class LightningGamePlayExtensionImpl implements LightningGamePlayExtension{
    
    @Override
    public BasePlaySession playLightning(BasePlaySession basePlaySession, UserInfo userInfo, String commandId,
            LogMessageBuilder logBuilder, Map<SlotConfigMode, ICommonSlotConfig> configMapper) {
        return basePlaySession;
    }

}
