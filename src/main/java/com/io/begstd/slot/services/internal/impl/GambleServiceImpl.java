package com.io.begstd.slot.services.internal.impl;

import com.io.begstd.extension.loader.ExtensionLoader;
import com.io.begstd.extension.loader.ExtensionManagerImpl;
import com.io.begstd.log.LogMessage;
import com.io.begstd.log.LogsUtils;
import com.io.begstd.slot.common.SlotGameError;
import com.io.begstd.slot.exception.SlotGameException;
import com.io.begstd.slot.machine.ISlotGameMachine;
import com.io.begstd.slot.machine.impl.BaseSlotMachineImpl;
import com.io.begstd.slot.model.app.BasePlaySession;
import com.io.begstd.slot.model.app.UserInfo;
import com.io.begstd.slot.model.config.ICommonSlotConfig;
import com.io.begstd.slot.model.config.ISlotConfigGamble;
import com.io.begstd.slot.model.config.ISlotMachineConfig;
import com.io.begstd.slot.model.config.SlotConfigMode;
import com.io.begstd.slot.model.domain.Money;
import com.io.begstd.slot.model.wallet.WalletPromotionDto;
import com.io.begstd.slot.services.extension.common.PlaySessionExtension;
import com.io.begstd.slot.services.extension.common.SubPlaySessionExtension;
import com.io.begstd.slot.services.extension.common.WalletExtension;
import com.io.begstd.slot.services.extension.gameplay.GambleEndExtension;
import com.io.begstd.slot.services.extension.gameplay.GamblePlayExtension;
import com.io.begstd.slot.services.extension.validator.ValidationExtension;
import com.io.begstd.slot.services.internal.BaseGambleGameService;
import com.io.begstd.slot.services.internal.GambleService;
import com.io.begstd.slot.services.internal.WalletTrialModeService;
import com.io.begstd.wallet.service.WalletService;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.util.Map;

public class GambleServiceImpl extends BaseGambleGameService implements GambleService {

    @Autowired
    WalletTrialModeService walletTrialModeService;
    
    @Autowired
    WalletService walletService;
    
    @Autowired
    ISlotGameMachine slotGameMachine;
    
    @Autowired
    private ExtensionLoader<ExtensionManagerImpl> extensionLoader;

    /**
     * openCell is id of SymbolGamble in SlotGame_Gamble_Config.json
     */
    @Override
    protected BasePlaySession execute(BasePlaySession basePlaySession, String commandId, UserInfo userInfo, int openCell,
                                      double totalBet, boolean isTrialMode,
                                      Map<SlotConfigMode, ICommonSlotConfig> configMapper) {
        
        ISlotMachineConfig slotMachineConfigNormal = (ISlotMachineConfig) configMapper.get(SlotConfigMode.NORMAL);
        ISlotConfigGamble slotMachineConfigGamble = (ISlotConfigGamble) configMapper.get(SlotConfigMode.GAMBLE);
        
        //get extension class
        ExtensionManagerImpl extensionManager = extensionLoader.getExtensionManager();
        ValidationExtension validationExtension = extensionManager.validationExtension();
        PlaySessionExtension playSessionExtension = extensionManager.playSessionExtension();
        SubPlaySessionExtension subPlaySession = extensionManager.subPlaySessionExtension();
        WalletExtension walletExtension = extensionManager.walletExtension();
        GamblePlayExtension gamblePlayExtension = extensionManager.gamblePlayExtension();
        GambleEndExtension gambleEndExtension = extensionManager.gambleEndExtension();
        //step 1: validation
        validationExtension.validateInGamble(basePlaySession, userInfo, commandId, totalBet, openCell, slotMachineConfigGamble);

        //log builder
        long lStartTimeGolbal = Instant.now().toEpochMilli();
        LogMessage.LogMessageBuilder logBuilder = LogMessage.builder();
        logBuilder
            .cmdId(commandId)
            .actorId(userInfo.userId())
            .serviceId(basePlaySession.serviceId())
            .psId(basePlaySession.uuid())
            .stateName("GambleServiceImpl.execute");
        
        // is gamble expired?
        if (gambleEndExtension.isEndGamble(basePlaySession, totalBet)) { // Call end Gamble Session
            return gamblePlayExtension.updateEndGamble(basePlaySession, totalBet, openCell, commandId, configMapper, userInfo);
        } else {
            if (!Money.of(totalBet).eq(basePlaySession.winAmount())) {
                throw new SlotGameException(SlotGameError.INVALID_TOTAL_BET, basePlaySession.userId(), basePlaySession.userType(), basePlaySession.commandId());
            }
            if (basePlaySession.gambleGameRemain() == 0) {
                throw new SlotGameException(SlotGameError.OUT_OF_SPIN_TURN, basePlaySession.userId(), basePlaySession.userType(), basePlaySession.commandId());
            }

            //step 2: update playsession
            BasePlaySession.BasePlaySessionBuilder<?,?> builder = playSessionExtension
                    .updatePlaySessionBuilderForGamble(basePlaySession, commandId, totalBet, openCell, configMapper, userInfo);
            //step 3: prepare data for gamble before spin
            basePlaySession = subPlaySession.setSubPlaySessionInfoGamble(builder.build(), configMapper);
            //step 4: minusWallet
            walletExtension.minusWallet(new WalletPromotionDto(
                builder.build(), logBuilder,
                slotMachineConfigNormal.totalCredit(), slotMachineConfigNormal.serviceCode(),
                slotMachineConfigNormal.serviceName(), slotMachineConfigNormal.prefixService(),
                walletTrialModeService, walletService, Money.of(totalBet), isTrialMode, null, null, null, userInfo));

            //Binding extension
            BaseSlotMachineImpl baseSlotMachine = (BaseSlotMachineImpl)slotGameMachine;
            baseSlotMachine.bindExtension(extensionManager);

            BasePlaySession playSessionAfterSpin = slotGameMachine.playGamble(basePlaySession, slotMachineConfigGamble);

            //UnBind extension
            baseSlotMachine.unBindExtension();
            
            int remain = builder.build().gambleGameRemain();
            logBuilder.stepName("End spin of gameble game").owner(LogMessage.OWNER_GAME)
                .message("Passed - gambleGameRemain: "+ remain)
                .timeExe(Instant.now().toEpochMilli() - lStartTimeGolbal);
            LogsUtils.writeLogInfo(logBuilder.build());
        
            return playSessionAfterSpin;
        }
    }


}
