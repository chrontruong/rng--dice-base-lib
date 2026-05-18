package com.io.begstd.slot.services.internal;

import com.io.begstd.log.LogMessage;
import com.io.begstd.log.LogsUtils;
import com.io.begstd.slot.common.SlotGameError;
import com.io.begstd.slot.exception.SlotGameException;
import com.io.begstd.slot.model.app.BasePlaySession;
import com.io.begstd.slot.model.app.UserInfo;
import com.io.begstd.slot.model.config.ICommonSlotConfig;
import com.io.begstd.slot.model.config.ISlotMachineConfig;
import com.io.begstd.slot.model.config.SlotConfigMode;
import com.io.begstd.slot.utils.ConfigManager;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

public abstract class BaseFreeGameService extends BaseSpinService {

    @Autowired
    private UserService userService;

    public BasePlaySession spin(String commandId, UserInfo userInfo) {
        return spin(commandId, userInfo, false);
    }
    
    public BasePlaySession spinTrial(String commandId, UserInfo userInfo) {
        return spin(commandId, userInfo, true);
    }
    
    private BasePlaySession spin(String commandId, UserInfo userInfo, boolean isTrialMode) {
//        checkServiceId(slotMachineConfigForFree.serviceId(), serviceId, userId, commandId);
        ISlotMachineConfig slotMachineConfigForNormal = (ISlotMachineConfig) configManager.getConfigMain(SlotConfigMode.NORMAL);
        ISlotMachineConfig slotMachineConfigForFree = (ISlotMachineConfig) configManager.getConfigMain(SlotConfigMode.FREE);

        //log builder
        LogMessage.LogMessageBuilder logBuilder = LogMessage.builder();
        logBuilder
            .cmdId(commandId)
            .actorId(userInfo.userId())
            .stateName("BaseFreeGameService.spin");

//        checkAuthorize(slotMachineConfigForFree.serviceId(), userId, commandId);
        BasePlaySession basePlaySession = userService.getPlaySession(slotMachineConfigForFree.serviceId(), userInfo);
        if(basePlaySession != null && !basePlaySession.isTrialMode() && isTrialMode) {
            logBuilder.serviceId(basePlaySession.serviceId())
                .psId(basePlaySession.uuid()).stepName("isTrialMode")
                .message("ERROR CALL SERVICE FREE GAME TRIAL MODE FOR A REAL PLAYSESSION FOR USER");
            LogsUtils.writeLogError(logBuilder.build());
            throw new SlotGameException(SlotGameError.UNEXPECTED, userInfo.userId(), basePlaySession.userType(), commandId);
        }
        if(basePlaySession != null && basePlaySession.isTrialMode() && !isTrialMode) {
            logBuilder.serviceId(basePlaySession.serviceId())
                .psId(basePlaySession.uuid()).stepName("isTrialMode")
                .message("ERROR CALL SERVICE FREE GAME MODE FOR A TRIAL PLAYSESSION FOR USER");
            LogsUtils.writeLogError(logBuilder.build());
            throw new SlotGameException(SlotGameError.UNEXPECTED, userInfo.userId(), basePlaySession.userType(), commandId);
        }
        if ((basePlaySession != null) && (basePlaySession.userId().equals(userInfo.userId()))) {
            BasePlaySession basePlaySessionAfterSpin = execute(basePlaySession, commandId, userInfo, configManager.getMainSlotConfigMap());
            return finishPlaySession(basePlaySessionAfterSpin, slotMachineConfigForNormal, userInfo);
        } else {
            if (basePlaySession != null) {
                logBuilder.serviceId(basePlaySession.serviceId())
                    .psId(basePlaySession.uuid()).stepName("Error UserID")
                    .message("ERROR LOGIN USER AND USERID IN PS " + basePlaySession.userId());
                LogsUtils.writeLogError(logBuilder.build());
                throw new SlotGameException(SlotGameError.UNEXPECTED, userInfo.userId(), basePlaySession.userType(), commandId);
            } else {
                throw new SlotGameException(SlotGameError.PLAYSESSION_NOT_EXISTED, userInfo.userId(), null, commandId);
            }
        }
    }

    protected abstract BasePlaySession execute(BasePlaySession basePlaySession, String commandId, UserInfo userInfo,
                                               Map<SlotConfigMode, ICommonSlotConfig> configMapper);
}
