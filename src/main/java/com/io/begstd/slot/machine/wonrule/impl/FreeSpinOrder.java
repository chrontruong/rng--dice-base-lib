package com.io.begstd.slot.machine.wonrule.impl;

import com.io.begstd.log.LogMessage;
import com.io.begstd.log.LogsUtils;
import com.io.begstd.slot.machine.wonrule.WonRuleExtension;
import com.io.begstd.slot.model.app.BasePlaySession;
import com.io.begstd.slot.model.args.CalculatePayoutArgs;
import com.io.begstd.slot.model.gamerule.Symbol;
import com.io.begstd.slot.model.gamerule.SymbolType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component("freeGameOrder")
@Slf4j
public class FreeSpinOrder implements WonRuleExtension {
    
    public static final String FREESPIN_ORDER = "freeGameOrder";
    
    @Override
    public String getName() {
        return FREESPIN_ORDER;
    }

    @Override
    public BasePlaySession calculatorPayout(CalculatePayoutArgs calculatePayoutArgs, BasePlaySession basePlaySession) {
        if(calculatePayoutArgs.config().reelCheckForFreeGame() == null) {
            log.warn("ReelCheckForFreeSpin is null in input parameter.");
            return basePlaySession;
        }
        
        int freeSpinAward = 1;
        Symbol[][] viewMatrix = calculatePayoutArgs.matrixScreen().horizontalMatrix();
        List<Integer> resultCheck = new ArrayList<Integer>();
        Symbol symScatter = null;
        //check reel based on field in config
        for (int reelIndex: calculatePayoutArgs.config().reelCheckForFreeGame()) {
            if ((reelIndex < calculatePayoutArgs.matrixScreen().reelSize()) && (reelIndex>=0)) {
                int found = 0;
                int row = 0;
                while ((row < calculatePayoutArgs.matrixScreen().rowSize()) && (found == 0)) {
                    if (SymbolType.SCATTER == viewMatrix[row][reelIndex].type()) {
                        found = 1;
                        symScatter = viewMatrix[row][reelIndex];
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
        //get freespin when win freespin
        int winFreeGame = 0;
        if (freeSpinAward > 0) {
            winFreeGame = symScatter.freespin().get(resultCheck.size()-1);
            
        }
        return updatePlaySession(basePlaySession, winFreeGame);
    }

    private BasePlaySession updatePlaySession(BasePlaySession basePlaySession, int wonCount) {
        BasePlaySession.BasePlaySessionBuilder<?,?> builder = basePlaySession.toBuilder();
        if (wonCount > 0) {
            builder.addFreeGameCount(wonCount);
        }
      //log
        LogMessage.LogMessageBuilder logBuilder = LogMessage.builder();
        logBuilder.cmdId(basePlaySession.commandId())
            .actorId(basePlaySession.userId())
            .serviceId(basePlaySession.serviceId())
            .psId(basePlaySession.uuid())
            .stateName("FreeSpinOrder")
            .stepName("updatePlaySession " + basePlaySession.state()).owner(LogMessage.OWNER_GAME)
            .message("addFreeGameCount = " + wonCount)
            .timeExe(0);
        LogsUtils.writeLogDebug(logBuilder.build());
        
        return builder.build();
    }
}
