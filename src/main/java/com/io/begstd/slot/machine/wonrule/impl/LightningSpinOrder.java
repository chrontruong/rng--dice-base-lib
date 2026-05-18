package com.io.begstd.slot.machine.wonrule.impl;

import com.io.begstd.slot.machine.wonrule.WonRuleExtension;
import com.io.begstd.slot.model.app.BasePlaySession;
import com.io.begstd.slot.model.app.GameState;
import com.io.begstd.slot.model.args.CalculatePayoutArgs;
import com.io.begstd.slot.model.gamerule.Symbol;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

@Component("lightningGameOrder")
@Slf4j
public class LightningSpinOrder implements WonRuleExtension {
    
    public static final String LIGHTNING_SPIN_ORDER = "lightningGameOrder";
    public static final String KNOROS_CODE = "2";
    
    @Override
    public String getName() {
        return LIGHTNING_SPIN_ORDER;
    }
    
    @Override
    public BasePlaySession calculatorPayout(CalculatePayoutArgs calculatePayoutArgs, BasePlaySession basePlaySession) {
        if(calculatePayoutArgs.config().reelCheckForLightningGame() == null) {
            log.warn("lightningGameOrder is null in input parameter.");
            return basePlaySession;
        }
        
        int lightningSpinAward = 1;
        Symbol[][] viewMatrix = calculatePayoutArgs.matrixScreen().horizontalMatrix();
        List<Integer> resultCheck = new ArrayList<Integer>();
        Symbol symScatter = null;
        //check reel based on field in config
        for (int reelIndex: calculatePayoutArgs.config().reelCheckForLightningGame()) {
            if ((reelIndex < calculatePayoutArgs.matrixScreen().reelSize()) && (reelIndex>=0)) {
                int found = 0;
                int row = 0;
                while ((row < calculatePayoutArgs.matrixScreen().rowSize())) {
                    if (!CollectionUtils.isEmpty(calculatePayoutArgs.config().getLightningSymbolCodes()) &&
                        calculatePayoutArgs.config().getLightningSymbolCodes().contains(viewMatrix[row][reelIndex].code())) {
                        found++;
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
            if(item < calculatePayoutArgs.config().numSymbolWinLightningGame()) {
                lightningSpinAward = 0;
                break;
            }
        }
        //get lightningspin when win lightningspin
        int winLightningGame = 0;
        if (lightningSpinAward > 0) {
            winLightningGame = calculatePayoutArgs.config().numPlayTotalInLightningGame();
        }
        return updatePlaySession(basePlaySession, winLightningGame);
    }

    private BasePlaySession updatePlaySession(BasePlaySession basePlaySession, int wonCount) {
        BasePlaySession.BasePlaySessionBuilder<?,?> builder = basePlaySession.toBuilder();
        if (wonCount > 0) {
            builder.addLightningGameCount(wonCount);
            if(basePlaySession.state() == GameState.NORMAL_GAME) {
                builder.resetLastFreeGameWinData();
                builder.freeGameRemain(0);
                builder.freeGameTotal(0);
            }
        }
        return builder.build();
    }
}
