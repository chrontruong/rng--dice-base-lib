package com.io.begstd.dice.services.internal;

import com.io.begstd.dice.model.app.BasePlaySession;
import com.io.begstd.dice.model.app.UserInfo;
import com.io.begstd.dice.model.config.ICommonDiceConfig;
import com.io.begstd.dice.model.config.IDiceMachineConfig;
import com.io.begstd.dice.model.config.DiceConfigMode;
import com.io.begstd.dice.command.SpinCmd;
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
        IDiceMachineConfig slotMachineConfigNormal = (IDiceMachineConfig) configManager.getConfigMain(DiceConfigMode.NORMAL);
        BasePlaySession basePlaySession = userService.getPlaySession(slotMachineConfigNormal.serviceId(), userInfo);

        if(basePlaySession != null && basePlaySession.isTrialMode() && !isTrialMode) {
            userService.removePlaySession(basePlaySession);
            basePlaySession = null;
        }
        if (!isTrialMode) {
            walletTrialModeService.removeUser(userInfo.userId());
            jackpotTrialModeService.removeUserJackpot(userInfo.userId(), slotMachineConfigNormal.JACKPOTS());
        }

        BasePlaySession basePlaySessionAfterSpin = execute(basePlaySession, commandId, userInfo, cmd, isTrialMode, configManager.getMainDiceConfigMap());
        return finishPlaySession(basePlaySessionAfterSpin, slotMachineConfigNormal, userInfo);
    }

    protected abstract BasePlaySession execute(BasePlaySession basePlaySession, String commandId, UserInfo userInfo, SpinCmd cmd,
                                               boolean isTrialMode, Map<DiceConfigMode, ICommonDiceConfig> configMapper);
}
