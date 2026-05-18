package com.io.begstd.slot.machine.wonrule.impl;

import com.io.begstd.log.LogMessage;
import com.io.begstd.log.LogsUtils;
import com.io.begstd.slot.machine.wonrule.WonRuleExtension;
import com.io.begstd.slot.model.app.BasePlaySession;
import com.io.begstd.slot.model.app.GameState;
import com.io.begstd.slot.model.app.JackpotInfo;
import com.io.begstd.slot.model.app.JackpotInfo.JackpotInfoBuilder;
import com.io.begstd.slot.model.args.CalculatePayoutArgs;
import com.io.begstd.slot.model.domain.Money;
import com.io.begstd.slot.model.gamerule.BettingLine;
import com.io.begstd.slot.model.gamerule.InitJackpotChild;
import com.io.begstd.slot.model.gamerule.Symbol;
import com.io.begstd.slot.services.external.JackpotService;
import com.io.begstd.slot.services.internal.JackpotTrialModeService;
import com.io.begstd.slot.utils.GameUtils;
import com.io.begstd.slot.utils.MatrixUtil;
import lombok.extern.slf4j.Slf4j;
import org.javatuples.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
@Slf4j
@Component("jpJackpotLine")
public class JPSymbolJackpotLine implements WonRuleExtension {
    
    public static final String JACKPOTLINE = "jpJackpotLine";
    @Autowired
    private Environment environment;
    
    @Override
    public String getName() {
        return JACKPOTLINE;
    }

    @Autowired
    private JackpotService jackpotService;

    @Autowired
    private JackpotTrialModeService jackpotTrialModeService;

    @Override
    public BasePlaySession calculatorPayout(CalculatePayoutArgs calculatePayoutArgs, BasePlaySession basePlaySession) {
        // check JackPot and calculate amount
        List<String> payLineList = new ArrayList<String>();
        for (BettingLine bettingItem : calculatePayoutArgs.bettingLines()) {
            Pair<String, Money> winJackpot = this.calculateWonByJackPot(calculatePayoutArgs, bettingItem, payLineList, basePlaySession);
            if (winJackpot.getValue1().isGreaterThan(Money.ZERO)) {
                if(Arrays.asList(environment.getActiveProfiles()).contains("rtpdebug")) {
                    MatrixUtil.logMatrixRTP(calculatePayoutArgs.matrixScreen().horizontalMatrixDataCell(), 
                        ("jackpotLine--"+ basePlaySession.uuid() +" ---state:"+ basePlaySession.state()+" ---winJackpot:"+winJackpot),
                        (basePlaySession.state() == GameState.NORMAL_GAME? basePlaySession.normalGameTableFormat():  basePlaySession.freeGameTableFormat()));
                }
                return updatePlaySession(basePlaySession, winJackpot, payLineList);
            }
        }
        return basePlaySession;
    }
    
    private BasePlaySession updatePlaySession(BasePlaySession basePlaySession, Pair<String, Money> winJackpot, List<String> payLineList) {
        BasePlaySession.BasePlaySessionBuilder<?,?> builder = basePlaySession.toBuilder();
        if (winJackpot.getValue1().isGreaterThan(Money.of(1))) {
            builder.addJackpotWinAmount(winJackpot.getValue1());
            String payLineString = GameUtils.getPayLineString(basePlaySession, payLineList);
            builder.addJackpotInfo(winJackpot.getValue0(), winJackpot.getValue1(), payLineString);
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
            .stateName("JackpotLine")
            .stepName("updatePlaySession " + basePlaySession.state()).owner(LogMessage.OWNER_GAME)
            .message("Jackpot amount = " + winJackpot.getValue1() + " - " + winJackpot.getValue0())
            .timeExe(0);
        LogsUtils.writeLogDebug(logBuilder.build());
        return builder.build();
    }

    private Pair<String, Money> calculateWonByJackPot(CalculatePayoutArgs calculatePayoutArgs, 
            BettingLine bettingLine, List<String> payLineList, BasePlaySession basePlaySession) {
        int countWild = 0;
        List<Symbol> rowMatchBetLine = calculatePayoutArgs.matrixScreen().findRowByPerLine(bettingLine.line());
        
        Symbol wild = null;
        for (Symbol item : rowMatchBetLine) {
            if ((item != null) && ("JP".equals(item.code()))) {
                countWild++;
                wild = item;
            } else {
                break;
            }
        }
        String jackpotId = "";
        Money amountItem = Money.ZERO;
        if (countWild >= calculatePayoutArgs.config().jackpotLineSize()) {
            
            // won JackPot, call JackPot service to get amount
            List<InitJackpotChild> jackpotKindConfig = calculatePayoutArgs.initJackpotList();
            double initAmountJackpot = 0.0;
            if (jackpotKindConfig != null && jackpotKindConfig.size() == 1) {
                initAmountJackpot = jackpotKindConfig.get(0).initAmount();
                jackpotId = jackpotKindConfig.get(0).code();
            }
            jackpotId = calculatePayoutArgs.denomLevel().jackpotID() + jackpotId;
            if(basePlaySession.isTrialMode()) {
                amountItem = jackpotTrialModeService.awardJackpot(basePlaySession.userId(), "cmdIdJpLTrialMode_" + Instant.now().toEpochMilli(),  jackpotId,
                        calculatePayoutArgs.denomLevel().amount().multiply(initAmountJackpot).value().doubleValue());
                log.debug("Win Jackpot line trial mode playsessionId: "+ basePlaySession.uuid()+" -- userId: "+ basePlaySession.userId()+" -- jackpotId:"+jackpotId+" amount: "+amountItem);
            } else {
                amountItem = jackpotService.awardJackpot("cmdIdJpL_" + Instant.now().toEpochMilli(),  jackpotId,
                        calculatePayoutArgs.denomLevel().amount().multiply(initAmountJackpot).value().doubleValue());
                log.debug("Win Jackpot line playsessionId: "+ basePlaySession.uuid()+" -- userId: "+ basePlaySession.userId()+" -- jackpotId:"+jackpotId+" amount: "+amountItem);
            }
            
            payLineList.add(bettingLine.line().id() + ";" + calculatePayoutArgs.config().jackpotLineSize()+";" + amountItem + ";" + wild.code());
            if(Arrays.asList(environment.getActiveProfiles()).contains("rtpdebug")) {
//              MatrixUtil.logMatrixRTP(calculatePayoutArgs.matrixScreen().horizontalMatrixDataCell(), "line", Arrays.asList(3,3,3,3,3));
                log.error("Line Id:"+bettingLine.line().id()+"-- Win jackpot :" + calculatePayoutArgs.config().jackpotLineSize()+";" + amountItem + ";" + wild.code());
            }
        }

        return new Pair<>(jackpotId, amountItem);
    }
}
