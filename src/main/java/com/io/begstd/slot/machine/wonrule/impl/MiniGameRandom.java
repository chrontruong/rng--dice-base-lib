package com.io.begstd.slot.machine.wonrule.impl;

import com.io.begstd.log.LogMessage;
import com.io.begstd.log.LogsUtils;
import com.io.begstd.slot.machine.wonrule.WonRuleExtension;
import com.io.begstd.slot.model.app.BasePlaySession;
import com.io.begstd.slot.model.args.CalculatePayoutArgs;
import com.io.begstd.slot.model.config.ISlotMachineConfig;
import com.io.begstd.slot.model.gamerule.Symbol;
import com.io.begstd.slot.model.gamerule.SymbolType;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.javatuples.Pair;
import org.springframework.stereotype.Component;

import java.util.Objects;
/**
 * 
 * Don't use for new game, new game with use BonusGameRandom rule
 *
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Component("bonusGameRandom")
public class MiniGameRandom implements WonRuleExtension {
    public static final String BONUSGAME_RANDOM = "bonusGameRandom";
    @Override
    public String getName() {
        return BONUSGAME_RANDOM;
    }

    @Override
    public BasePlaySession calculatorPayout(CalculatePayoutArgs calculatePayoutArgs, BasePlaySession basePlaySession) {
        Pair<Symbol, Integer> bonusCount = calculatePayoutArgs.matrixScreen().countSymbolByType(SymbolType.BONUS);
        int winBonus = 0;
        if (Objects.nonNull(bonusCount.getValue0())) {
            if (bonusCount.getValue1() >= calculatePayoutArgs.config().numSymbolWinBonusGame()) {
                if (bonusCount.getValue0().paytable() != null) {
                    int sizePayTable = bonusCount.getValue0().paytable().size();
                    int pos = bonusCount.getValue1() >  sizePayTable?sizePayTable:bonusCount.getValue1();
                    winBonus = bonusCount.getValue0().paytable().get(pos - 1);
                }
            }
        }
        
        return updatePlaySession(basePlaySession, calculatePayoutArgs.config(), winBonus);
    }

    private BasePlaySession updatePlaySession(BasePlaySession basePlaySession, ISlotMachineConfig config, int wonCount) {
        BasePlaySession.BasePlaySessionBuilder<?,?> builder = basePlaySession.toBuilder();

        if (wonCount > 0) {
            builder.addBonusGameCount(wonCount);
            builder.bonusPlayRemain(config.numPlayTotalInBonusGame());
        }
        LogMessage.LogMessageBuilder logBuilder = LogMessage.builder();
        logBuilder.cmdId(basePlaySession.commandId())
            .actorId(basePlaySession.userId())
            .serviceId(basePlaySession.serviceId())
            .psId(basePlaySession.uuid())
            .stateName("MiniGameRandom")
            .stepName("updatePlaySession " + basePlaySession.state()).owner(LogMessage.OWNER_GAME)
            .message("addBonusGameCount = " + wonCount)
            .timeExe(0);
        LogsUtils.writeLogDebug(logBuilder.build()); 
        
        return builder.build();
    }
}
