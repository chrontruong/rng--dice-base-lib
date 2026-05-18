package com.io.begstd.slot.services.internal.impl;

import com.io.begstd.extension.loader.ExtensionLoader;
import com.io.begstd.extension.loader.ExtensionManagerImpl;
import com.io.begstd.log.LogMessage;
import com.io.begstd.log.LogsUtils;
import com.io.begstd.slot.model.app.BasePlaySession;
import com.io.begstd.slot.model.app.UserInfo;
import com.io.begstd.slot.model.config.ICommonSlotConfig;
import com.io.begstd.slot.model.config.IMiniSlotConfig;
import com.io.begstd.slot.model.config.SlotConfigMode;
import com.io.begstd.slot.services.extension.common.PlaySessionExtension;
import com.io.begstd.slot.services.extension.gameplay.BonusGamePlayExtension;
import com.io.begstd.slot.services.extension.validator.ValidationExtension;
import com.io.begstd.slot.services.internal.BaseMiniGameService;
import com.io.begstd.slot.services.internal.MiniGameService;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.util.Map;

public final class MiniGameServiceImpl extends BaseMiniGameService implements MiniGameService {

    public static float DEFAULT_VALE = -1;

    @Autowired
    private ExtensionLoader<ExtensionManagerImpl> extensionLoader;

    @Override
    public BasePlaySession execute(BasePlaySession basePlaySession, String commandId, UserInfo userInfo, int openCell,
                                   Map<SlotConfigMode, ICommonSlotConfig> configMapper) {
        ExtensionManagerImpl extensionManager = extensionLoader.getExtensionManager();
        PlaySessionExtension playSessionExtension = extensionManager.playSessionExtension();
        BonusGamePlayExtension bonusGamePlayExtension = extensionManager.bonusGamePlayExtension();
        ValidationExtension validationExtension = extensionManager.validationExtension();

        validationExtension.validateInBonus(basePlaySession, userInfo, commandId, openCell, (IMiniSlotConfig) configMapper.get(SlotConfigMode.MINI));

        //log builder
        long lStartTimeGolbal = Instant.now().toEpochMilli();
        LogMessage.LogMessageBuilder logBuilder = LogMessage.builder();
        logBuilder
            .cmdId(commandId)
            .actorId(userInfo.userId())
            .serviceId(basePlaySession.serviceId())
            .psId(basePlaySession.uuid())
            .stateName("MiniGameServiceImpl.execute");

        BasePlaySession.BasePlaySessionBuilder<?,?> builder = playSessionExtension.updatePlaySessionBuilderForBonus(basePlaySession, commandId, userInfo);

        BasePlaySession playSessionAfterPlayBonus = bonusGamePlayExtension.playBonus(
                builder.build(), userInfo, commandId, openCell, logBuilder, configMapper);

        logBuilder.timeExe(Instant.now().toEpochMilli() - lStartTimeGolbal);
        LogsUtils.writeLogInfo(logBuilder.build());
        // check finish
        return playSessionAfterPlayBonus;
    }
}
