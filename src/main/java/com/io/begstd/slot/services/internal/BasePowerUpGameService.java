package com.io.begstd.slot.services.internal;

import com.io.begstd.slot.common.SlotGameError;
import com.io.begstd.slot.exception.SlotGameException;
import com.io.begstd.slot.model.app.BasePlaySession;
import com.io.begstd.slot.model.app.UserInfo;
import com.io.begstd.slot.model.config.ICommonSlotConfig;
import com.io.begstd.slot.model.config.ISlotMachineConfig;
import com.io.begstd.slot.model.config.SlotConfigMode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

@Slf4j
public abstract class BasePowerUpGameService extends BaseSpinService {

    @Autowired
    private UserService userService;

    public BasePlaySession spin(String commandId, UserInfo userInfo, int selectedOption) {
        return spin(commandId, userInfo, selectedOption, false);
    }
    
    public BasePlaySession spinTrial(String commandId, UserInfo userInfo, int selectedOption) {
        return spin(commandId, userInfo, selectedOption, true);
    }
    
    private BasePlaySession spin(String commandId, UserInfo userInfo, int selectedOption, boolean isTrialMode) {
//        checkServiceId(slotMachineConfigNormal.serviceId(), serviceId, userId, commandId);
//        checkAuthorize(slotMachineConfigNormal.serviceId(), userId, commandId);
        ISlotMachineConfig slotMachineConfigNormal = (ISlotMachineConfig) configManager.getConfigMain(SlotConfigMode.NORMAL);
        BasePlaySession basePlaySession = userService.getPlaySession(slotMachineConfigNormal.serviceId(), userInfo);
        
        if(basePlaySession != null && !basePlaySession.isTrialMode() && isTrialMode) {
            log.error("{} ERROR CALL SERVICE POWERUP GAME TRIAL MODE FOR A REAL PLAYSESSION FOR USER {}", commandId, userInfo.userId());
            throw new SlotGameException(SlotGameError.UNEXPECTED, userInfo.userId(), basePlaySession.userType(), commandId);
        }
        if(basePlaySession != null && basePlaySession.isTrialMode() && !isTrialMode) {
            log.error("{} ERROR CALL SERVICE POWERUP GAME TRIAL MODE FOR A TRIAL PLAYSESSION FOR USER {}", commandId, userInfo.userId());
            throw new SlotGameException(SlotGameError.UNEXPECTED, userInfo.userId(), basePlaySession.userType(), commandId);
        }
        
        if ((basePlaySession != null) && (basePlaySession.userId().equals(userInfo.userId()))) {
            BasePlaySession basePlaySessionAfterSpin = execute(basePlaySession, commandId, userInfo, selectedOption, configManager.getMainSlotConfigMap());
            return finishPlaySession(basePlaySessionAfterSpin, slotMachineConfigNormal, userInfo);
        } else {
            if (basePlaySession != null) {
                log.error("{} ERROR LOGIN USER {} AND USERID IN PS {}", commandId, userInfo.userId(), basePlaySession.userId());
                throw new SlotGameException(SlotGameError.UNEXPECTED, userInfo.userId(), basePlaySession.userType(), commandId);
            } else {
                log.error("{} ERROR LOGIN USER {} AND USERID IN PS {}", commandId, userInfo.userId(), "null");
                throw new SlotGameException(SlotGameError.PLAYSESSION_NOT_EXISTED, userInfo.userId(), null, commandId);
            }
        }
    }

    protected abstract BasePlaySession execute(BasePlaySession basePlaySession, String commandId, UserInfo userInfo, int selectedOption,
                                               Map<SlotConfigMode, ICommonSlotConfig> configMapper);
}
