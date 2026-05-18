package com.io.begstd.slot.services.internal.impl;

import com.io.begstd.extension.loader.ExtensionLoader;
import com.io.begstd.extension.loader.ExtensionManagerImpl;
import com.io.begstd.log.LogMessage;
import com.io.begstd.log.LogsUtils;
import com.io.begstd.slot.machine.ISlotGameMachine;
import com.io.begstd.slot.machine.impl.BaseSlotMachineImpl;
import com.io.begstd.slot.model.app.BasePlaySession;
import com.io.begstd.slot.model.app.UserInfo;
import com.io.begstd.slot.model.config.ICommonSlotConfig;
import com.io.begstd.slot.model.config.SlotConfigMode;
import com.io.begstd.slot.services.extension.common.PlaySessionExtension;
import com.io.begstd.slot.services.extension.common.SpinListenerExtension;
import com.io.begstd.slot.services.extension.common.SubPlaySessionExtension;
import com.io.begstd.slot.services.extension.validator.ValidationExtension;
import com.io.begstd.slot.services.internal.BaseFreeGameService;
import com.io.begstd.slot.services.internal.FreeGameService;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.util.Map;


public final class FreeGameServiceImpl extends BaseFreeGameService implements FreeGameService {

    @Autowired
    private ISlotGameMachine slotGameMachine;

    @Autowired
    private ExtensionLoader<ExtensionManagerImpl> extensionLoader;

    @Override
    public BasePlaySession execute(BasePlaySession basePlaySession, String commandId, UserInfo userInfo,
                                   Map<SlotConfigMode, ICommonSlotConfig> configMapper) {
        //log builder
        long lStartTimeGolbal = Instant.now().toEpochMilli();
        LogMessage.LogMessageBuilder logBuilder = LogMessage.builder();
        logBuilder
            .cmdId(commandId)
            .actorId(userInfo.userId())
            .serviceId(basePlaySession.serviceId())
            .psId(basePlaySession.uuid())
            .stateName("FreeGameServiceImpl.execute");

        ExtensionManagerImpl extensionManager = extensionLoader.getExtensionManager();
        PlaySessionExtension playSessionExtension = extensionManager.playSessionExtension();
        SubPlaySessionExtension subPlaySession = extensionManager.subPlaySessionExtension();
        ValidationExtension validationExtension = extensionManager.validationExtension();
        SpinListenerExtension spinListenerExtension = extensionManager.spinListenerExtension();

        //step 1: validate
        validationExtension.validateInFree(basePlaySession, userInfo, commandId);
        //step 2: update playsession
        BasePlaySession.BasePlaySessionBuilder<?,?> builder = playSessionExtension.updatePlaySessionBuilderForFree(basePlaySession, commandId, userInfo);
        
        //step 3: prepare data for free game before spin
        basePlaySession = subPlaySession.setSubPlaySessionInfoFree(builder.build(), configMapper);

        //Binding extension
        BaseSlotMachineImpl baseSlotMachine = (BaseSlotMachineImpl)slotGameMachine;
        baseSlotMachine.bindExtension(extensionManager);

        BasePlaySession basePlaySessionAfterSpin = slotGameMachine.spin(basePlaySession, configMapper);

        //UnBind extension
        baseSlotMachine.unBindExtension();
        
        logBuilder.stepName("End").owner(LogMessage.OWNER_GAME).message("Passed - Played free spin")
        .timeExe(Instant.now().toEpochMilli() - lStartTimeGolbal);

        LogsUtils.writeLogInfo(logBuilder.build());
        // step 4: doAfterSpin
        return spinListenerExtension.onAfterSpin(basePlaySessionAfterSpin, configMapper);
    }
}
