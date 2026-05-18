package com.io.begstd.slot.services.internal.impl;

import com.io.begstd.extension.loader.ExtensionLoader;
import com.io.begstd.extension.loader.ExtensionManagerImpl;
import com.io.begstd.log.LogMessage;
import com.io.begstd.log.LogsUtils;
import com.io.begstd.slot.model.app.BasePlaySession;
import com.io.begstd.slot.model.app.UserInfo;
import com.io.begstd.slot.model.config.ICommonSlotConfig;
import com.io.begstd.slot.model.config.ISlotMachineConfig;
import com.io.begstd.slot.model.config.SlotConfigMode;
import com.io.begstd.slot.model.playsession.FreeGameProb;
import com.io.begstd.slot.services.extension.common.PlaySessionExtension;
import com.io.begstd.slot.services.extension.gameplay.FreeGameOptionPlayExtension;
import com.io.begstd.slot.services.extension.validator.ValidationExtension;
import com.io.begstd.slot.services.internal.BaseFreeSpinOptionGameService;
import com.io.begstd.slot.services.internal.FreeSpinOptionGameService;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.util.Map;


public final class FreeSpinOptionGameServiceImpl extends BaseFreeSpinOptionGameService implements FreeSpinOptionGameService {

    @Autowired
    private ExtensionLoader<ExtensionManagerImpl> extensionLoader;

    @Override
    public BasePlaySession execute(BasePlaySession basePlaySession, String commandId, UserInfo userInfo, int selectedOption,
                                   Map<SlotConfigMode, ICommonSlotConfig> configMapper) {
        
        ISlotMachineConfig slotMachineConfigNormal = (ISlotMachineConfig) configMapper.get(SlotConfigMode.NORMAL);
        ExtensionManagerImpl extensionManager = extensionLoader.getExtensionManager();
        PlaySessionExtension playSessionExtension = extensionManager.playSessionExtension();
        ValidationExtension validationExtension = extensionManager.validationExtension();
        FreeGameOptionPlayExtension freeGameOptionPlayExtension = extensionManager.freeGameOptionPlayExtension();

        validationExtension.validateInFreeSpinOption(basePlaySession, userInfo, commandId, selectedOption);
        //log builder
        long lStartTimeGolbal = Instant.now().toEpochMilli();
        LogMessage.LogMessageBuilder logBuilder = LogMessage.builder();
        logBuilder
            .cmdId(commandId)
            .actorId(userInfo.userId())
            .serviceId(basePlaySession.serviceId())
            .psId(basePlaySession.uuid())
            .stateName("FreeSpinOptionGameServiceImpl.execute");
        
        BasePlaySession.BasePlaySessionBuilder<?,?> builder = playSessionExtension.updatePlaySessionBuilderForFreeOption(basePlaySession, commandId, userInfo);

        BasePlaySession psResult = freeGameOptionPlayExtension.playFreeGameOption(builder.build(), userInfo, commandId, selectedOption, slotMachineConfigNormal, logBuilder);

        logBuilder.stepName("End").owner(LogMessage.OWNER_GAME).message("Passed - addFreeGameCount:" + ((FreeGameProb)psResult.freeSpinProb()).wonCount())
        .timeExe(Instant.now().toEpochMilli() - lStartTimeGolbal);
        LogsUtils.writeLogInfo(logBuilder.build());
        return psResult;
    }
}
