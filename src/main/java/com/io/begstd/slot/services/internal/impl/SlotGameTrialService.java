package com.io.begstd.slot.services.internal.impl;

import com.io.begstd.extension.loader.ExtensionLoader;
import com.io.begstd.extension.loader.ExtensionManagerImpl;
import com.io.begstd.slot.model.app.BasePlaySession;
import com.io.begstd.slot.model.app.UserInfo;
import com.io.begstd.slot.model.config.ISlotMachineConfig;
import com.io.begstd.slot.model.config.SlotConfigMode;
import com.io.begstd.slot.repository.impl.RedisPlaySessionRepositoryImpl;
import com.io.begstd.slot.services.extension.common.ResumeExtension;
import com.io.begstd.slot.services.internal.BaseTrialService;
import com.io.begstd.slot.services.internal.JackpotTrialModeService;
import com.io.begstd.slot.services.internal.WalletTrialModeService;
import com.io.begstd.slot.utils.BeanUtils;
import com.io.begstd.slot.utils.ConfigManager;
import org.springframework.beans.factory.annotation.Autowired;

public class SlotGameTrialService implements BaseTrialService {

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
        ISlotMachineConfig slotMachineConfig = (ISlotMachineConfig) configManager.getConfigMain(SlotConfigMode.NORMAL);
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
        ISlotMachineConfig slotMachineConfig = (ISlotMachineConfig) configManager.getConfigMain(SlotConfigMode.NORMAL);
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
