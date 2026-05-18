package com.io.begstd.slot.services.extension.gameplay.impl;

import com.io.begstd.log.LogMessage.LogMessageBuilder;
import com.io.begstd.slot.model.app.BasePlaySession;
import com.io.begstd.slot.model.app.UserInfo;
import com.io.begstd.slot.model.config.ICommonSlotConfig;
import com.io.begstd.slot.model.config.SlotConfigMode;
import com.io.begstd.slot.services.extension.gameplay.PowerUpGamePlayExtension;

import java.util.Map;

public class PowerUpGamePlayExtensionImpl implements PowerUpGamePlayExtension{
    
    @Override
    public BasePlaySession playPowerUp(BasePlaySession basePlaySession, UserInfo userInfo, String commandId,
            int openCell, LogMessageBuilder logBuilder, Map<SlotConfigMode, ICommonSlotConfig> configMapper) {

        return basePlaySession;
    }

    

}
