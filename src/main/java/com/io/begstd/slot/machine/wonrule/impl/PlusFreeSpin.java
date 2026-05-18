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

/**
 * This class returns scatter number in matrix
 */
@Component("freeGamePlus")
class PlusFreeSpin implements WonRuleExtension {
    public static final String FREESPIN_PLUS = "freeGamePlus";
    
    @Override
    public String getName() {
        return FREESPIN_PLUS;
    }

    @Override
    public BasePlaySession calculatorPayout(CalculatePayoutArgs calculatePayoutArgs, BasePlaySession basePlaySession) {
        // Check scatter and having freespin.
        int freeSpinAward = 0;
        Pair<Symbol, Integer> scatterAndCount = calculatePayoutArgs.matrixScreen().countSymbolByType(SymbolType.SCATTER);
        if (Objects.nonNull(scatterAndCount.getValue0())) {
            freeSpinAward = scatterAndCount.getValue1();
        }

        return updatePlaySession(basePlaySession, freeSpinAward);
    }

    private BasePlaySession updatePlaySession(BasePlaySession basePlaySession, int wonCount) {
        BasePlaySession.BasePlaySessionBuilder builder = basePlaySession.toBuilder();
        if (wonCount > 0) {
            builder.addFreeGameCount(wonCount);
        }
        LogMessage.LogMessageBuilder logBuilder = LogMessage.builder();
        logBuilder.cmdId(basePlaySession.commandId())
            .actorId(basePlaySession.userId())
            .serviceId(basePlaySession.serviceId())
            .psId(basePlaySession.uuid())
            .stateName("PlusFreeSpin")
            .stepName("updatePlaySession " + basePlaySession.state()).owner(LogMessage.OWNER_GAME)
            .message("addFreeGameCount = " + wonCount)
            .timeExe(0);
        LogsUtils.writeLogDebug(logBuilder.build()); 
        return builder.build();
    }
}
