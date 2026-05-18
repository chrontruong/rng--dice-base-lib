package com.io.begstd.slot.services.internal;

import com.io.begstd.log.LogMessage;
import com.io.begstd.log.LogsUtils;
import com.io.begstd.slot.common.SlotGameError;
import com.io.begstd.slot.exception.SlotGameException;
import com.io.begstd.slot.model.app.BasePlaySession;
import com.io.begstd.slot.model.app.UserInfo;
import com.io.begstd.slot.model.config.ICommonSlotConfig;
import com.io.begstd.slot.model.config.IMiniSlotConfig;
import com.io.begstd.slot.model.config.ISlotMachineConfig;
import com.io.begstd.slot.model.config.SlotConfigMode;
import com.io.begstd.slot.utils.ConfigManager;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

public abstract class BaseMiniGameService extends BaseSpinService {

    @Autowired
    private UserService userService;

    public BasePlaySession play(String commandId, UserInfo userInfo, int openCell) {
        return play(commandId, userInfo, openCell, false);
    }
    
    public BasePlaySession playTrial(String commandId, UserInfo userInfo, int openCell) {
        return play(commandId, userInfo, openCell, true);
    }
    
    private BasePlaySession play(String commandId, UserInfo userInfo, int openCell, boolean isTrialMode) {
//        checkServiceId(serviceId, userId, commandId);
        ISlotMachineConfig slotMachineConfigNormal = (ISlotMachineConfig) configManager.getConfigMain(SlotConfigMode.NORMAL);
        IMiniSlotConfig miniSlotConfig = (IMiniSlotConfig) configManager.getConfigMain(SlotConfigMode.MINI);

        //log builder
        LogMessage.LogMessageBuilder logBuilder = LogMessage.builder();
        logBuilder
            .cmdId(commandId)
            .actorId(userInfo.userId())
            .stateName("BaseMiniGameService.spin");
        
        BasePlaySession basePlaySession = userService.getPlaySession(miniSlotConfig.serviceId(), userInfo);

        if(basePlaySession != null && !basePlaySession.isTrialMode() && isTrialMode) {
            logBuilder.serviceId(basePlaySession.serviceId())
                .psId(basePlaySession.uuid()).stepName("isTrialMode")
                .message("ERROR CALL SERVICE MINI TRIAL MODE FOR A REAL PLAYSESSION FOR USER");
            LogsUtils.writeLogError(logBuilder.build());

            throw new SlotGameException(SlotGameError.UNEXPECTED, userInfo.userId(), basePlaySession.userType(), commandId);
        }
        if(basePlaySession != null && basePlaySession.isTrialMode() && !isTrialMode) {
            logBuilder.serviceId(basePlaySession.serviceId())
                .psId(basePlaySession.uuid()).stepName("isTrialMode")
                .message("ERROR CALL SERVICE MINI MODE FOR A TRIAL PLAYSESSION FOR USER");
            LogsUtils.writeLogError(logBuilder.build());

            throw new SlotGameException(SlotGameError.UNEXPECTED, userInfo.userId(), basePlaySession.userType(), commandId);
        }
        
        if ((basePlaySession != null) && (basePlaySession.userId().equals(userInfo.userId()))) {
            BasePlaySession basePlaySessionAfterSpin = execute(basePlaySession, commandId, userInfo, openCell, configManager.getMainSlotConfigMap());
            return finishPlaySession(basePlaySessionAfterSpin, slotMachineConfigNormal, userInfo);
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
    public abstract BasePlaySession execute(BasePlaySession basePlaySession, String commandId, UserInfo userInfo, int openCell,
                                            Map<SlotConfigMode, ICommonSlotConfig> configMapper);
}
