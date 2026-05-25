package com.io.begstd.dice.services.internal.impl;

import com.io.begstd.dice.services.internal.BaseNormalGameService;
import com.io.begstd.dice.services.internal.JackpotTrialModeService;
import com.io.begstd.extension.loader.ExtensionLoader;
import com.io.begstd.extension.loader.ExtensionManagerImpl;
import com.io.begstd.log.LogMessage;
import com.io.begstd.log.LogsUtils;
import com.io.begstd.dice.machine.IDiceGameMachine;
import com.io.begstd.dice.machine.impl.BaseDiceMachineImpl;
import com.io.begstd.dice.model.app.BasePlaySession;
import com.io.begstd.dice.model.app.Promotion;
import com.io.begstd.dice.model.app.UserInfo;
import com.io.begstd.dice.model.config.ICommonDiceConfig;
import com.io.begstd.dice.model.config.IDiceMachineConfig;
import com.io.begstd.dice.model.config.DiceConfigMode;
import com.io.begstd.dice.model.domain.Money;
import com.io.begstd.dice.model.gamerule.DenominationLevel;
import com.io.begstd.dice.model.wallet.WalletPromotionDto;
import com.io.begstd.dice.repository.PromotionRepositoryService;
import com.io.begstd.dice.services.extension.common.DenominationExtension;
import com.io.begstd.dice.services.extension.common.JackpotExtension;
import com.io.begstd.dice.services.extension.common.PlaySessionExtension;
import com.io.begstd.dice.services.extension.common.PromotionExtension;
import com.io.begstd.dice.services.extension.common.SpinListenerExtension;
import com.io.begstd.dice.services.extension.common.SubPlaySessionExtension;
import com.io.begstd.dice.services.extension.common.WalletExtension;
import com.io.begstd.dice.services.external.JackpotService;
import com.io.begstd.dice.services.external.PromotionService;
import com.io.begstd.dice.services.internal.NormalGameService;
import com.io.begstd.dice.services.internal.WalletTrialModeService;
import com.io.begstd.dice.utils.GameUtils;
import com.io.begstd.dice.command.ExtraBetLevelCmd;
import com.io.begstd.dice.command.SpinCmd;
import com.io.begstd.dice.common.DiceGameError;
import com.io.begstd.dice.exception.DiceGameException;
import com.io.begstd.wallet.service.WalletService;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;


@FieldDefaults(level = AccessLevel.PRIVATE)
public final class NormalGameServiceImpl extends BaseNormalGameService implements NormalGameService {

    @Autowired
    JackpotService jackpotService;

    @Autowired
    WalletService walletService;

    @Autowired
    IDiceGameMachine slotGameMachine;

    @Autowired
    PromotionService promotionService;

    @Autowired
    PromotionRepositoryService promotionRepository;

    @Autowired
    WalletTrialModeService walletTrialModeService;

    @Autowired
    JackpotTrialModeService jackpotTrialModeService;

    @Autowired
    ExtensionLoader<ExtensionManagerImpl> extensionLoader;

    /**
     * Spin logic step by step
     * Step0: init PlaySessionBuilder
     * Step1: get promotion
     * Step2: extract DenominationLevel & ExtraBet
     * Step3: extract BettingLine
     * Step4: calculate totalBet
     * Step5: set subPlaySession
     * Step6: update wallet
     * Step7: distribute jackpot
     * Step8: spin machine
     * Step9: after spin logic
     */
    @Override
    public BasePlaySession execute(BasePlaySession curBasePlaySession, String commandId, UserInfo userInfo,
                                   SpinCmd cmd, boolean isTrialMode, Map<DiceConfigMode, ICommonDiceConfig> configMapper) {
        if (curBasePlaySession != null) {
            throw new DiceGameException(DiceGameError.INVALID_COMMAND, userInfo.userId(), userInfo.userType(), commandId);
        }
        IDiceMachineConfig slotMachineConfigNormal = (IDiceMachineConfig) configMapper.get(DiceConfigMode.NORMAL);

        ExtensionManagerImpl extensionManager = extensionLoader.getExtensionManager();
        PlaySessionExtension playSessionExtension  = extensionManager.playSessionExtension();
        PromotionExtension promotionExtension = extensionManager.promotionExtension();
        DenominationExtension denominationExtension = extensionManager.denominationExtension();
        SubPlaySessionExtension subPlaySessionExtension = extensionManager.subPlaySessionExtension();
        WalletExtension walletExtension = extensionManager.walletExtension();
        JackpotExtension jackpotExtension = extensionManager.jackpotExtension();
        SpinListenerExtension spinListenerExtension = extensionManager.spinListenerExtension();


        long lStartTimeNormal = Instant.now().toEpochMilli();
        UUID uuid = UUID.randomUUID();
        String serviceId = slotMachineConfigNormal.serviceId();
        LogMessage.LogMessageBuilder logBuilder = LogMessage.builder();
        logBuilder.cmdId(commandId)
                .actorId(userInfo.userId())
                .serviceId(slotMachineConfigNormal.serviceId())
                .psId(uuid.toString())
                .stateName("NormalGameServiceImpl.execute");

        //Step0
        BasePlaySession.BasePlaySessionBuilder<?,?> builder = playSessionExtension.createPlaySessionBuilderForNormal(serviceId, uuid.toString(), commandId, userInfo, cmd, isTrialMode);
        Promotion promotion = null;
        //Step1
        if (!isTrialMode) {
            promotion = promotionExtension.getPromotion(promotionRepository, logBuilder, serviceId, userInfo, cmd, commandId);
        }//Step2
        DenominationLevel betDemonPerTotalCredit = cmd.toBetPerLineModel(slotMachineConfigNormal, builder.build());

        ExtraBetLevelCmd extraBet = cmd.toExtraBet(slotMachineConfigNormal, builder.build());

        builder.betDenom(betDemonPerTotalCredit);
        builder.extraDenom(extraBet);
        builder.currency(slotMachineConfigNormal.getAvailableCurrency(betDemonPerTotalCredit.curr()).name());
        builder.lang(cmd.lang());
        //Step4
        Money bettingTotal = denominationExtension.convertToTotalBet(builder.build(), slotMachineConfigNormal);

        builder.totalBet(bettingTotal);
        //step5
        BasePlaySession afterSubUpdated = subPlaySessionExtension.setSubPlaySessionInfoNormal(builder.build(), configMapper);
        //Step6
        BasePlaySession afterMinusPS = walletExtension.minusWalletWithPromotion(new WalletPromotionDto(
                afterSubUpdated, logBuilder,
                slotMachineConfigNormal.totalCredit(), slotMachineConfigNormal.serviceCode(),
                slotMachineConfigNormal.serviceName(), slotMachineConfigNormal.prefixService(),
                walletTrialModeService, walletService, afterSubUpdated.totalBet(), isTrialMode, promotion, promotionRepository, promotionService, userInfo
        ));
        //Step7
        Map<String, Money> progressiveJackpotGroup = jackpotExtension.distributeJackPots(slotMachineConfigNormal,
                betDemonPerTotalCredit, afterSubUpdated.totalBet(), extraBet);
        String resPlusJackpot = GameUtils.progressiveJackpot(userInfo.userId(), commandId, logBuilder, jackpotService,
                jackpotTrialModeService, progressiveJackpotGroup, isTrialMode);

        //Binding extension
        BaseDiceMachineImpl baseDiceMachine = (BaseDiceMachineImpl)slotGameMachine;
        baseDiceMachine.bindExtension(extensionManager);

        //Step8
        BasePlaySession basePlaySessionAfterSpin = slotGameMachine.spin(afterMinusPS, configMapper);

        //UnBind extension
        baseDiceMachine.unBindExtension();

        BasePlaySession.BasePlaySessionBuilder<?,?> builderAfter = basePlaySessionAfterSpin.toBuilder();
        if (!isTrialMode) {
            builderAfter.jpInfoAmt(resPlusJackpot);
        }

        logBuilder.stepName("End").owner(LogMessage.OWNER_GAME).message("Passed - Played normal spin")
                .timeExe(Instant.now().toEpochMilli() - lStartTimeNormal);
        LogsUtils.writeLogInfo(logBuilder.build());
        //Step10
        return spinListenerExtension.onAfterSpin(builderAfter.build(), configMapper);
    }

}
