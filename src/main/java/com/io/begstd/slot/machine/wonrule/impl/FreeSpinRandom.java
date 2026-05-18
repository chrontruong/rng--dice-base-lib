package com.io.begstd.slot.machine.wonrule.impl;

import com.io.begstd.log.LogMessage;
import com.io.begstd.log.LogsUtils;
import com.io.begstd.slot.machine.wonrule.WonRuleExtension;
import com.io.begstd.slot.model.app.BasePlaySession;
import com.io.begstd.slot.model.args.CalculatePayoutArgs;
import com.io.begstd.slot.model.gamerule.Symbol;
import com.io.begstd.slot.model.gamerule.SymbolType;
import org.javatuples.Pair;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component("freeGameRandom")
public class FreeSpinRandom implements WonRuleExtension {
    public static final String FREESPIN_RANDOM = "freeGameRandom";
    
    @Override
    public String getName() {
        return FREESPIN_RANDOM;
    }

    @Override
    public BasePlaySession calculatorPayout(CalculatePayoutArgs calculatePayoutArgs, BasePlaySession basePlaySession) {
        // Check scatter and having freespin.
        int freeSpinAward = 0;
        Pair<Symbol, Integer> scatterAndCount = calculatePayoutArgs.matrixScreen().countSymbolByType(SymbolType.SCATTER);
        if (Objects.nonNull(scatterAndCount.getValue0()) &&
            Objects.nonNull( scatterAndCount.getValue0().freespin())) {
            int freeSpinSize = scatterAndCount.getValue0().freespin().size();
            int pos = scatterAndCount.getValue1() > freeSpinSize ? freeSpinSize:scatterAndCount.getValue1();
            freeSpinAward = scatterAndCount.getValue0().freespin().get(pos - 1);
        }

        return updatePlaySession(basePlaySession, freeSpinAward);
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
            .stateName("FreeSpinRandom")
            .stepName("updatePlaySession " + basePlaySession.state()).owner(LogMessage.OWNER_GAME)
            .message("addFreeGameCount = " + wonCount)
            .timeExe(0);
        LogsUtils.writeLogDebug(logBuilder.build());
        
        return builder.build();
    }
}
