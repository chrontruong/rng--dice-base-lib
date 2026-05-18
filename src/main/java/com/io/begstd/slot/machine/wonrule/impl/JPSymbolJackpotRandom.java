package com.io.begstd.slot.machine.wonrule.impl;

import com.io.begstd.log.LogMessage;
import com.io.begstd.log.LogsUtils;
import com.io.begstd.slot.machine.wonrule.WonRuleExtension;
import com.io.begstd.slot.model.app.BasePlaySession;
import com.io.begstd.slot.model.app.JackpotInfo;
import com.io.begstd.slot.model.app.JackpotInfo.JackpotInfoBuilder;
import com.io.begstd.slot.model.args.CalculatePayoutArgs;
import com.io.begstd.slot.model.domain.Money;
import com.io.begstd.slot.model.gamerule.InitJackpotChild;
import com.io.begstd.slot.model.gamerule.Symbol;
import com.io.begstd.slot.services.external.JackpotService;
import com.io.begstd.slot.services.internal.JackpotTrialModeService;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.javatuples.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Component("jpJackpotRandom")
public class JPSymbolJackpotRandom implements WonRuleExtension {
    public static final String JP_JACKPOTRANDOM = "jpJackpotRandom";
    
    @Override
    public String getName() {
        return JP_JACKPOTRANDOM;
    }

    @Autowired
    private JackpotService jackpotService;
    
    @Autowired
    private JackpotTrialModeService jackpotTrialModeService;
    
    @Override
    public BasePlaySession calculatorPayout(CalculatePayoutArgs calculatePayoutArgs, BasePlaySession basePlaySession) {
        // check JackPot and calculate amount
        List<String> payLineList = new ArrayList<String>();
        Pair<String, Money> winJackpot = this.calculateWonJackpot(calculatePayoutArgs, payLineList, basePlaySession);
        return updatePlaySession(basePlaySession, winJackpot, payLineList);
    }

    private BasePlaySession updatePlaySession(BasePlaySession basePlaySession, Pair<String, Money> winJackpot, List<String> payLineList) {
        
        BasePlaySession.BasePlaySessionBuilder<?,?> builder = basePlaySession.toBuilder();
        if (winJackpot.getValue1().isGreaterThan(Money.of(1))) {
            builder.addJackpotWinAmount(winJackpot.getValue1());
            builder.addJackpotInfo(winJackpot.getValue0(), winJackpot.getValue1(), null);
//            builder.jackpotId(winJackpot.getValue0());
            builder.jackpotTotalCount(basePlaySession.jackpotTotalCount() + 1);
//            builder.payLineJackpot(payLineList);
            JackpotInfoBuilder jpBuilder = JackpotInfo.builder();
            jpBuilder.jackpotAmount(winJackpot.getValue1());
            jpBuilder.jackpotId(winJackpot.getValue0());
            jpBuilder.winTime(Instant.now().toEpochMilli());
            jpBuilder.userId(basePlaySession.userId());
            jpBuilder.state(basePlaySession.state());
            builder.addJackpotHistory(jpBuilder.build());
            
        }
        LogMessage.LogMessageBuilder logBuilder = LogMessage.builder();
        logBuilder.cmdId(basePlaySession.commandId())
            .actorId(basePlaySession.userId())
            .serviceId(basePlaySession.serviceId())
            .psId(basePlaySession.uuid())
            .stateName("JPJackpotRandom")
            .stepName("updatePlaySession " + basePlaySession.state()).owner(LogMessage.OWNER_GAME)
            .message("Jackpot amount = " + winJackpot.getValue1() + " - " + winJackpot.getValue0() +" - isTrialMode: "+ basePlaySession.isTrialMode())
            .timeExe(0);
        LogsUtils.writeLogDebug(logBuilder.build());
        return builder.build();
    }
    /*
     * this method calculates won jackpot amount with existing wild in matrix
     */

    private Pair<String, Money> calculateWonJackpot(CalculatePayoutArgs calculatePayoutArgs, List<String> payLineList, BasePlaySession basePlaySession) {
        
        Symbol[][] viewMatrix = calculatePayoutArgs.matrixScreen().horizontalMatrix();
        Money amountResult = Money.ZERO;
        int countWild = 0;
        for (int row = 0; row < calculatePayoutArgs.matrixScreen().rowSize(); row++) {
            for (int col = 0; col < calculatePayoutArgs.matrixScreen().reelSize(); col++) {
                if ((viewMatrix[row][col] != null) && ("JP".equals(viewMatrix[row][col].code()))) {
                   countWild +=1;
                }
            }
        }
        String jackpotId = "";
        if (countWild >= calculatePayoutArgs.config().jackpotLineSize()) {
            List<InitJackpotChild> jackpotKindConfig = calculatePayoutArgs.initJackpotList();
            double initAmountJackpot = 0.0;
            if (jackpotKindConfig != null && jackpotKindConfig.size() == 1) {
                initAmountJackpot = jackpotKindConfig.get(0).initAmount();
                jackpotId = jackpotKindConfig.get(0).code();
            }
            jackpotId = calculatePayoutArgs.denomLevel().jackpotID() + jackpotId;

            if(basePlaySession.isTrialMode()) {
                amountResult = jackpotTrialModeService.awardJackpot(basePlaySession.userId(), "commandIdJackPotRandomTrial",  jackpotId,
                        calculatePayoutArgs.denomLevel().amount().multiply(initAmountJackpot).value().doubleValue());
//                log.info("Win Jackpot Random trial mode playsessionId: "+playSession.uuid()+" -- userId: "+playSession.userId()+" -- jackpotId:"+jackpotId+" amount: "+amountResult);
            } else {
                amountResult = jackpotService.awardJackpot("commandIdJackPotRandomReal",  jackpotId,
                        calculatePayoutArgs.denomLevel().amount().multiply(initAmountJackpot).value().doubleValue());
//                log.info("Win Jackpot Random playsessionId: "+playSession.uuid()+" -- userId: "+playSession.userId()+" -- jackpotId:"+jackpotId+" amount: "+amountResult);
            }
            payLineList.add(0 + ";" + calculatePayoutArgs.config().jackpotLineSize()+";" + amountResult + ";JP");
        }
        return new Pair<>(jackpotId, amountResult);
    }
}
