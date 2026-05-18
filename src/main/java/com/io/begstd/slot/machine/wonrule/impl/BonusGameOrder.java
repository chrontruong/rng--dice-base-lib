package com.io.begstd.slot.machine.wonrule.impl;

import com.io.begstd.log.LogMessage;
import com.io.begstd.log.LogsUtils;
import com.io.begstd.slot.machine.wonrule.WonRuleExtension;
import com.io.begstd.slot.model.app.BasePlaySession;
import com.io.begstd.slot.model.args.CalculatePayoutArgs;
import com.io.begstd.slot.model.config.ISlotMachineConfig;
import com.io.begstd.slot.model.gamerule.Symbol;
import com.io.begstd.slot.model.gamerule.SymbolType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 
 * Will use bonusgame properties in bonus symbol to replace for paytable of bonus symbol in MiniGameRandom
 *
 */
@Component("bonusSymbolOrder")
@Slf4j
public class BonusGameOrder implements WonRuleExtension {
    public static final String BONUSGAME_ORDER = "bonusSymbolOrder";
    @Override
    public String getName() {
        return BONUSGAME_ORDER;
    }

    @Override
    public BasePlaySession calculatorPayout(CalculatePayoutArgs calculatePayoutArgs, BasePlaySession basePlaySession) {
        if(calculatePayoutArgs.config().reelCheckForBonusGame() == null) {
            log.warn("ReelCheckForMini is null in input parameter.");
            return basePlaySession;
        }
        int freeSpinAward = 1;
        Symbol[][] viewMatrix = calculatePayoutArgs.matrixScreen().horizontalMatrix();
        List<Integer> resultCheck = new ArrayList<Integer>();
        Symbol symBonus = null;
        //check reel 1
        for (int reelInput: calculatePayoutArgs.config().reelCheckForBonusGame()) {
            int reelIndex = reelInput - 1;
            if ((reelIndex < calculatePayoutArgs.matrixScreen().reelSize()) && (reelIndex>=0)) {
                int found = 0;
                int row = 0;
                while ((row < calculatePayoutArgs.matrixScreen().rowSize()) && (found == 0)) {
                    if (Objects.nonNull(viewMatrix[row][reelIndex]) && SymbolType.BONUS == viewMatrix[row][reelIndex].type()) {
                        symBonus = viewMatrix[row][reelIndex];
                        found = 1;
                    }
                    row ++;
                }
                resultCheck.add(found);
                if (found == 0) {
                    break;
                }
            }
        }
        // check result all reel
        for (int item : resultCheck) {
            freeSpinAward *= item;
        }
        int winBonus = 0;
        if (freeSpinAward == 1) {
            //win bonus
            if (symBonus.bonusgame() != null) {
                winBonus = symBonus.bonusgame().get(resultCheck.size() - 1);
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
            .stateName("MiniGameOrder")
            .stepName("updatePlaySession " + basePlaySession.state()).owner(LogMessage.OWNER_GAME)
            .message("addBonusGameCount = " + wonCount)
            .timeExe(0);
        LogsUtils.writeLogDebug(logBuilder.build()); 
        
        return builder.build();
    }
}
