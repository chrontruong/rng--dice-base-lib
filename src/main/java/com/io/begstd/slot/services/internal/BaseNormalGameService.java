package com.io.begstd.slot.services.internal;

import com.io.begstd.slot.command.SpinCmd;
import com.io.begstd.slot.model.app.BasePlaySession;
import com.io.begstd.slot.model.app.UserInfo;
import com.io.begstd.slot.model.config.ICommonSlotConfig;
import com.io.begstd.slot.model.config.ISlotMachineConfig;
import com.io.begstd.slot.model.config.SlotConfigMode;
import com.io.begstd.slot.utils.ConfigManager;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

public abstract class BaseNormalGameService extends BaseSpinService {

    @Autowired
    private UserService userService;

    @Autowired
    private WalletTrialModeService walletTrialModeService;
    
    @Autowired
    private JackpotTrialModeService jackpotTrialModeService;
    
    public BasePlaySession spin(String commandId, UserInfo userInfo, SpinCmd cmd) {
        return spin(commandId, userInfo, cmd, false);
    }
    
    public BasePlaySession spinTrial(String commandId, UserInfo userInfo, SpinCmd cmd) {
        return spin(commandId, userInfo, cmd, true);
    }
    
    private BasePlaySession spin(String commandId, UserInfo userInfo, SpinCmd cmd, boolean isTrialMode) {
//        checkServiceId(slotMachineConfigNormal.serviceId(), serviceId, userId, commandId);
//        checkAuthorize(slotMachineConfigNormal.serviceId(), userId, commandId);
        ISlotMachineConfig slotMachineConfigNormal = (ISlotMachineConfig) configManager.getConfigMain(SlotConfigMode.NORMAL);
        BasePlaySession basePlaySession = userService.getPlaySession(slotMachineConfigNormal.serviceId(), userInfo);

        if(basePlaySession != null && basePlaySession.isTrialMode() && !isTrialMode) {
            userService.removePlaySession(basePlaySession);
            basePlaySession = null;
        }
        if(!isTrialMode) {
            walletTrialModeService.removeUser(userInfo.userId());
            jackpotTrialModeService.removeUserJackpot(userInfo.userId(), slotMachineConfigNormal.JACKPOTS());
        }

        // delete gamble playsession if exists
        BasePlaySession gamblePS = userService.getGamblePlaySession(slotMachineConfigNormal.serviceId(), userInfo);
        if (gamblePS != null) {
            userService.removeGamblePlaySession(gamblePS);
        }

        BasePlaySession basePlaySessionAfterSpin = execute(basePlaySession, commandId, userInfo, cmd, isTrialMode, configManager.getMainSlotConfigMap());
        return finishPlaySession(basePlaySessionAfterSpin, slotMachineConfigNormal, userInfo);
    }
    
    protected abstract BasePlaySession execute(BasePlaySession basePlaySession, String commandId, UserInfo userInfo, SpinCmd cmd,
                                               boolean isTrialMode, Map<SlotConfigMode, ICommonSlotConfig> configMapper);
}
