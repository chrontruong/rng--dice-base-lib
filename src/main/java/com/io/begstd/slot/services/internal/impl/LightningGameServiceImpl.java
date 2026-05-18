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
import com.io.begstd.slot.services.extension.gameplay.LightningGamePlayExtension;
import com.io.begstd.slot.services.extension.validator.ValidationExtension;
import com.io.begstd.slot.services.internal.BaseLightningGameService;
import com.io.begstd.slot.services.internal.LightningGameService;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.util.Map;


public final class LightningGameServiceImpl extends BaseLightningGameService implements LightningGameService {
    
    @Autowired
    private ExtensionLoader<ExtensionManagerImpl> extensionLoader;

    @Override
    public BasePlaySession execute(BasePlaySession basePlaySession, String commandId, UserInfo userInfo,
                                   Map<SlotConfigMode, ICommonSlotConfig> configMapper) {
        ExtensionManagerImpl extensionManager = extensionLoader.getExtensionManager();
        PlaySessionExtension playSessionExtension = extensionManager.playSessionExtension();
        ValidationExtension validationExtension = extensionManager.validationExtension();
        LightningGamePlayExtension lightningGamePlayExtension = extensionManager.lightningGamePlayExtension();

        validationExtension.validateInLightning(basePlaySession, userInfo, commandId);
            
        //log builder
        long lStartTimeGolbal = Instant.now().toEpochMilli();
        LogMessage.LogMessageBuilder logBuilder = LogMessage.builder();
        logBuilder
            .cmdId(commandId)
            .actorId(userInfo.userId())
            .serviceId(basePlaySession.serviceId())
            .psId(basePlaySession.uuid())
            .stateName("LightningGameServiceImpl.execute");

        BasePlaySession.BasePlaySessionBuilder<?,?> builder = playSessionExtension.updatePlaySessionBuilderForLightning(basePlaySession, commandId, userInfo);
        BasePlaySession afterPlay = lightningGamePlayExtension.playLightning(builder.build(), userInfo, commandId, logBuilder, configMapper);

        logBuilder.stepName("End spin of lighting game").owner(LogMessage.OWNER_GAME)
        .message("Passed - "+ afterPlay.lightningGameRemain())
        .timeExe(Instant.now().toEpochMilli() - lStartTimeGolbal);
        LogsUtils.writeLogInfo(logBuilder.build());
        
        return afterPlay;
    }
}
