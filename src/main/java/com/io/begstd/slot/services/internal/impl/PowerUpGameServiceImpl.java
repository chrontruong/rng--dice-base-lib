package com.io.begstd.slot.services.internal.impl;

import com.io.begstd.extension.loader.ExtensionLoader;
import com.io.begstd.extension.loader.ExtensionManagerImpl;
import com.io.begstd.log.LogMessage;
import com.io.begstd.log.LogsUtils;
import com.io.begstd.slot.model.app.BasePlaySession;
import com.io.begstd.slot.model.app.UserInfo;
import com.io.begstd.slot.model.config.ICommonSlotConfig;
import com.io.begstd.slot.model.config.SlotConfigMode;
import com.io.begstd.slot.services.extension.common.PlaySessionExtension;
import com.io.begstd.slot.services.extension.gameplay.PowerUpGamePlayExtension;
import com.io.begstd.slot.services.extension.validator.ValidationExtension;
import com.io.begstd.slot.services.internal.BasePowerUpGameService;
import com.io.begstd.slot.services.internal.PowerUpGameService;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.util.Map;

public final class PowerUpGameServiceImpl extends BasePowerUpGameService implements PowerUpGameService {

    @Autowired
    private ExtensionLoader<ExtensionManagerImpl> extensionLoader;
    
    @Override
    public BasePlaySession execute(BasePlaySession basePlaySession, String commandId, UserInfo userInfo, int openCell,
                                   Map<SlotConfigMode, ICommonSlotConfig> configMapper) {
        ExtensionManagerImpl extensionManager = extensionLoader.getExtensionManager();
        PlaySessionExtension playSessionExtension = extensionManager.playSessionExtension();
        PowerUpGamePlayExtension powerUpGamePlayExtension = extensionManager.powerUpGamePlayExtension();
        ValidationExtension validationExtension = extensionManager.validationExtension();

        validationExtension.validateInPowerUp(basePlaySession, userInfo, commandId);
        long lStartTimeGolbal = Instant.now().toEpochMilli();
        LogMessage.LogMessageBuilder logBuilder = LogMessage.builder();
        logBuilder
            .cmdId(commandId)
            .serviceId(basePlaySession.serviceId())
            .psId(basePlaySession != null ? basePlaySession.uuid() :"")
            .psId(basePlaySession.uuid())
            .stateName("StarWarPowerUpGameServiceImpl.execute");
        
        BasePlaySession.BasePlaySessionBuilder<?,?> builder = playSessionExtension.updatePlaySessionBuilderForPowerUp(basePlaySession, commandId, userInfo);
        BasePlaySession afterPlay = powerUpGamePlayExtension.playPowerUp(builder.build(), userInfo, commandId, openCell, logBuilder, configMapper);

        logBuilder.stepName("End spin of power up game").owner(LogMessage.OWNER_GAME)
        .message("Passed - "+ afterPlay.powerUpGameRemain())
        .timeExe(Instant.now().toEpochMilli() - lStartTimeGolbal);
        LogsUtils.writeLogInfo(logBuilder.build());
        
        return afterPlay;
    }
}
