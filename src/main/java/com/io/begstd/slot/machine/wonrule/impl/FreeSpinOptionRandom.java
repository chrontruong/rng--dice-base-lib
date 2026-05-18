package com.io.begstd.slot.machine.wonrule.impl;

import com.io.begstd.log.LogMessage;
import com.io.begstd.log.LogsUtils;
import com.io.begstd.slot.machine.wonrule.WonRuleExtension;
import com.io.begstd.slot.model.app.BasePlaySession;
import com.io.begstd.slot.model.app.GameState;
import com.io.begstd.slot.model.args.CalculatePayoutArgs;
import com.io.begstd.slot.model.config.ISlotMachineConfig;
import com.io.begstd.slot.model.gamerule.Symbol;
import com.io.begstd.slot.model.gamerule.SymbolType;
import org.javatuples.Pair;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component("freeOptionGameRandom")
public class FreeSpinOptionRandom implements WonRuleExtension {
    public static final String FREE_OPTION_RANDOM = "freeOptionGameRandom";
    
    @Override
    public String getName() {
        return FREE_OPTION_RANDOM;
    }

//    @Override
//    public PayoutCount calculatorPayout(CalculatePayoutArgs calculatePayoutArgs) {
//        // Check scatter and having freespin.
//        int freeSpinAward = 0;
//        Pair<Symbol, Integer> scatterAndCount = calculatePayoutArgs.matrixScreen().countSymbolByType(SymbolType.SCATTER);
//        if (Objects.nonNull(scatterAndCount.getValue0()) &&
//                scatterAndCount.getValue1() >= calculatePayoutArgs.numSymbolWinFreeGame()) {
//            freeSpinAward = 1;
//        }
//
//        return new PayoutCount(freeSpinAward);
//    }
    
    @Override
    public BasePlaySession calculatorPayout(CalculatePayoutArgs calculatePayoutArgs, BasePlaySession basePlaySession) {
        // Check scatter and having freespin.
        int freeSpinAward = 0;
        Pair<Symbol, Integer> scatterAndCount = calculatePayoutArgs.matrixScreen().countSymbolByType(SymbolType.SCATTER);
        if (Objects.nonNull(scatterAndCount.getValue0()) &&
                scatterAndCount.getValue1() >= calculatePayoutArgs.config().numSymbolWinFreeGame()) {
            freeSpinAward = 1;
        }

        return updatePlaySession(basePlaySession, freeSpinAward, calculatePayoutArgs.config());
    }
    
    private BasePlaySession updatePlaySession(BasePlaySession basePlaySession, int wonCount, ISlotMachineConfig config) {
        BasePlaySession.BasePlaySessionBuilder<?,?> builder = basePlaySession.toBuilder();
      //log
        LogMessage.LogMessageBuilder logBuilder = LogMessage.builder();
        logBuilder.cmdId(basePlaySession.commandId())
            .actorId(basePlaySession.userId())
            .serviceId(basePlaySession.serviceId())
            .psId(basePlaySession.uuid())
            .stateName("freeOptionGameRandom")
            .stepName("updatePlaySession " + basePlaySession.state()).owner(LogMessage.OWNER_GAME);
        if (wonCount > 0) {
            if (basePlaySession.state() == GameState.NORMAL_GAME) {
                builder.freeGameOption(config.getFreeSpinOptin());
                logBuilder.message("freeGameOption= " + config.getFreeSpinOptin());
            }
        } else {
            logBuilder.message("wonCount = " + wonCount);
        }
        logBuilder.timeExe(0);
        LogsUtils.writeLogDebug(logBuilder.build());
        
        return builder.build(); 
    }
}
