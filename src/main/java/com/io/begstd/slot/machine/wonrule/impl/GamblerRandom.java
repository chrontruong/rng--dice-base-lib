package com.io.begstd.slot.machine.wonrule.impl;

import com.io.begstd.log.LogMessage;
import com.io.begstd.log.LogsUtils;
import com.io.begstd.slot.config.StartTestProperty;
import com.io.begstd.slot.machine.wonrule.WonRuleExtension;
import com.io.begstd.slot.model.app.BasePlaySession;
import com.io.begstd.slot.model.args.CalculatePayoutArgs;
import com.io.begstd.slot.model.config.ICommonSlotConfig;
import com.io.begstd.slot.model.config.ISlotConfigGamble;
import com.io.begstd.slot.model.config.SlotConfigMode;
import com.io.begstd.slot.model.domain.Money;
import com.io.begstd.slot.model.gamerule.RangeGamblerValue;
import com.io.begstd.slot.services.internal.IRandomService;
import com.io.begstd.slot.utils.BeanUtils;
import com.io.begstd.slot.utils.ConfigManager;
import com.io.begstd.slot.utils.GameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;

@Component(GamblerRandom.WON_RULE_NAME)
public class GamblerRandom implements WonRuleExtension {

    public static final String WON_RULE_NAME = "gamblerRandom";

    @Autowired
    private StartTestProperty startTestProperty;

    private IRandomService getRandomService() {
        return BeanUtils.getBean(IRandomService.class);
    }
    @Override
    public BasePlaySession calculatorPayout(CalculatePayoutArgs calculatePayoutArgs, BasePlaySession playSession) {

        long lStartTimeGolbal = Instant.now().toEpochMilli();
        LogMessage.LogMessageBuilder logBuilder = LogMessage.builder();
        logBuilder
            .cmdId(playSession.commandId())
            .actorId(playSession.userId())
            .serviceId(playSession.serviceId())
            .psId(playSession.uuid())
            .owner(LogMessage.OWNER_GAME)
            .stateName("GamblerRandom.calculatorPayout");
        
        ConfigManager configManager = BeanUtils.getBean(ConfigManager.class);
        ISlotConfigGamble slotMachineConfigGamble = (ISlotConfigGamble) configManager.getConfigMain(SlotConfigMode.GAMBLE);

        Integer sysSelectedvalue = null;

        RangeGamblerValue rangeGamblerValue = slotMachineConfigGamble.getRangeGamblerById(playSession.gamblerUserSymbol().id());
        Integer maxRange = rangeGamblerValue.range().get(rangeGamblerValue.range().size() - 1);
        int idxRandom = getRandomService().random("GAMBLE", Integer.class, playSession.userId(), maxRange, playSession.uuid());
        int valueIndex = GameUtils.randomValueInRange(rangeGamblerValue.range(), idxRandom);
        sysSelectedvalue = rangeGamblerValue.value().get(valueIndex);

        logBuilder.stepName("Random gamble value - isTrial: " + playSession.isTrialMode())
                .message("sysSelectedvalue: " + sysSelectedvalue)
                .timeExe(Instant.now().toEpochMilli() - lStartTimeGolbal);
        LogsUtils.writeLogDebug(logBuilder.build());

        Money wonAmount = Money.ZERO;
        if (sysSelectedvalue == playSession.gamblerUserSymbol().id()) {
            double gamblerMultiply = slotMachineConfigGamble.gamblerMultiply();
            wonAmount = playSession.gamblerUserBet().multiply(gamblerMultiply);
        }
        return updatePlaySession(playSession, wonAmount, sysSelectedvalue, slotMachineConfigGamble);
    }

    private BasePlaySession updatePlaySession(BasePlaySession playSession, Money wonAmount, 
            int sysSelectedvalue, ISlotConfigGamble slotMachineConfigGamble) {
        
        BasePlaySession.BasePlaySessionBuilder<?,?> playSessionBuilder = playSession.toBuilder();
        
        playSessionBuilder.gamblerSystemSymbol(
                slotMachineConfigGamble.getSymbolById(sysSelectedvalue));
        
        if (wonAmount.gt(Money.ZERO)) {
            //win
            playSessionBuilder.winAmount(wonAmount);
        } else {
            //fail
            playSessionBuilder.gambleGameRemain(0);
            playSessionBuilder.winAmount(Money.ZERO);
        }
        return playSessionBuilder.build();
    }

    @Override
    public String getName() {
        return WON_RULE_NAME;
    }

    
}
