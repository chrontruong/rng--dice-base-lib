package com.io.begstd.dice.services.internal.impl;

import com.io.begstd.dice.services.internal.JackpotTrialModeService;
import com.io.begstd.extension.loader.ExtensionLoader;
import com.io.begstd.extension.loader.ExtensionManagerImpl;
import com.io.begstd.dice.model.app.BasePlaySession;
import com.io.begstd.dice.model.app.UserInfo;
import com.io.begstd.dice.model.config.IDiceMachineConfig;
import com.io.begstd.dice.model.config.DiceConfigMode;
import com.io.begstd.dice.repository.impl.RedisPlaySessionRepositoryImpl;
import com.io.begstd.dice.services.extension.common.ResumeExtension;
import com.io.begstd.dice.services.internal.BaseTrialService;
import com.io.begstd.dice.services.internal.WalletTrialModeService;
import com.io.begstd.dice.utils.BeanUtils;
import com.io.begstd.dice.utils.ConfigManager;
import org.springframework.beans.factory.annotation.Autowired;

public class DiceGameTrialService implements BaseTrialService {

    @Autowired
    private ExtensionLoader<ExtensionManagerImpl> extensionLoader;

    @Autowired
    private ConfigManager configManager;

    @Autowired
    private WalletTrialModeService walletTrialModeService;

    @Autowired
    private JackpotTrialModeService jackpotTrialModeService;

    @Override
    public void clearPlaySession(UserInfo userInfo) {
        RedisPlaySessionRepositoryImpl playSessionRepository = BeanUtils.getBean(RedisPlaySessionRepositoryImpl.class);
        IDiceMachineConfig slotMachineConfig = (IDiceMachineConfig) configManager.getConfigMain(DiceConfigMode.NORMAL);
        ExtensionManagerImpl extensionManager = extensionLoader.getExtensionManager();
        ResumeExtension resumeExtension = extensionManager.resumeExtension();
        BasePlaySession basePlaySession = resumeExtension.getPlaySession(playSessionRepository, slotMachineConfig.serviceId(),
                userInfo.userId(), userInfo.currency());
        if (basePlaySession != null) {
            if (basePlaySession.isTrialMode()) {
                playSessionRepository.removePlaySession(basePlaySession);
            }
        }
        walletTrialModeService.removeUser(userInfo.userId());
        jackpotTrialModeService.removeUserJackpot(userInfo.userId(), slotMachineConfig.getJackpotListForTrial());
    }

    @Override
    public void clearProcessBeforeSpin(UserInfo userInfo) {
        RedisPlaySessionRepositoryImpl playSessionRepository = BeanUtils.getBean(RedisPlaySessionRepositoryImpl.class);
        IDiceMachineConfig slotMachineConfig = (IDiceMachineConfig) configManager.getConfigMain(DiceConfigMode.NORMAL);
        ExtensionManagerImpl extensionManager = extensionLoader.getExtensionManager();
        ResumeExtension resumeExtension = extensionManager.resumeExtension();
        BasePlaySession basePlaySession = resumeExtension.getPlaySession(playSessionRepository, slotMachineConfig.serviceId(),
                userInfo.userId(), userInfo.currency());
        if (basePlaySession != null) {
            if (basePlaySession.isTrialMode()) {
                playSessionRepository.removePlaySession(basePlaySession);
            }
        }
        walletTrialModeService.removeUser(userInfo.userId());
        jackpotTrialModeService.removeUserJackpot(userInfo.userId(), slotMachineConfig.getJackpotListForTrial());
    }

    @Override
    public void clearProcessAfterSpin(UserInfo userInfo) {

    }
}
