package com.io.begstd.slot.machine.wonrule.impl;

import com.io.begstd.log.LogMessage;
import com.io.begstd.log.LogsUtils;
import com.io.begstd.slot.machine.wonrule.WonRuleExtension;
import com.io.begstd.slot.model.app.BasePlaySession;
import com.io.begstd.slot.model.app.GameState;
import com.io.begstd.slot.model.args.CalculatePayoutArgs;
import com.io.begstd.slot.model.domain.Money;
import com.io.begstd.slot.model.gamerule.BettingLine;
import com.io.begstd.slot.model.gamerule.Symbol;
import com.io.begstd.slot.model.gamerule.SymbolType;
import com.io.begstd.slot.utils.GameUtils;
import org.javatuples.Pair;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Calculate award with comparing WILD paytable and symbol win paytable in the same payline,
 * after that won paytable will multiply multipler param
 */
@Component("lineMultiplerHasWild")
public class LinesMultiplierHasPaytableWild implements WonRuleExtension {
    public static final String LINE_MULTIPLIER_HAS_WILD = "lineMultiplerHasWild";

    @Override
    public String getName() {
        return LINE_MULTIPLIER_HAS_WILD;
    }

    @Override
    public BasePlaySession calculatorPayout(CalculatePayoutArgs calculatePayoutArgs, BasePlaySession basePlaySession) {
        List<String> payLineList = new ArrayList<String>();
        Money totalWon = calculatePayoutArgs.bettingLines().stream().
                map(bettingLine -> calculateWonBy(bettingLine, calculatePayoutArgs, payLineList, basePlaySession)).reduce(Money::add).get();
        return updatePlaySession(basePlaySession, totalWon, payLineList);
    }
    
    private BasePlaySession updatePlaySession(BasePlaySession basePlaySession, Money wonAmount, List<String> payLineList) {
        BasePlaySession.BasePlaySessionBuilder<?,?> builder = basePlaySession.toBuilder();
        if (wonAmount.isGreaterThan(Money.ZERO)) {

            if (basePlaySession.state() == GameState.NORMAL_GAME) {
                builder.addNormalGameWinAmount(wonAmount);

                List<String> existedData = builder.build().normalGamePayLines();
                if (existedData != null) {
                    payLineList.addAll(existedData);
                }
                builder.normalGamePayLines(payLineList);
            } else if (basePlaySession.state() == GameState.FREE_GAME){
                builder.addFreeGameWinAmount(wonAmount);

                List<String> existedData = builder.build().freeGamePayLines();
                if (existedData != null) {
                    payLineList.addAll(existedData);
                }
                builder.freeGamePayLines(payLineList);
            } else {
                LogMessage.LogMessageBuilder logBuilder = LogMessage.builder();
                logBuilder.cmdId(basePlaySession.commandId())
                    .actorId(basePlaySession.userId())
                    .serviceId(basePlaySession.serviceId())
                    .psId(basePlaySession.uuid())
                    .stateName("LinesMultiplierHasPaytableWild")
                    .stepName("updatePlaySession " + basePlaySession.state()).owner(LogMessage.OWNER_GAME)
                    .message("Money can't update in this state " + basePlaySession.state())
                    .timeExe(0);
                LogsUtils.writeLogError(logBuilder.build());
//                log.error("Money can't update in this state="+playSession.state());
            }
        }
        return builder.build();
    }

    private Money calculateWonBy(BettingLine bettingLine, CalculatePayoutArgs calculatePayoutArgs, List<String> payLineList, BasePlaySession basePlaySession) {

        Money amountLine = Money.ZERO;
        List<Symbol> rowMatchBetLine = calculatePayoutArgs.matrixScreen().findRowByPerLine(bettingLine.line());
        Pair<Symbol, Integer> pairWild = null;
        Pair<Symbol, Integer> pairSymbol = null;
        Pair<Symbol, Integer> wonPair = null;
        if (rowMatchBetLine.get(0).type() == SymbolType.WILD) {
            pairWild = calculatePayoutArgs.matrixScreen().countSymbolInLineBySymbolType(rowMatchBetLine, SymbolType.WILD);
            pairSymbol = calculatePayoutArgs.matrixScreen().countSymbolInLine(rowMatchBetLine);
        } else {
            pairSymbol = calculatePayoutArgs.matrixScreen().countSymbolInLine(rowMatchBetLine);
        }
        BigDecimal wonRateForWild = null;
        BigDecimal wonRateForSymbol = null;
        
        if ((pairWild != null) && (Objects.nonNull(pairWild.getValue0()))) {
            if (pairWild.getValue0().paytable() != null) 
                wonRateForWild = new BigDecimal(pairWild.getValue0().paytable().get(pairWild.getValue1()));
        }
        if (Objects.nonNull(pairSymbol.getValue0())) {
            if (pairSymbol.getValue0().paytable() != null)
                wonRateForSymbol = new BigDecimal(pairSymbol.getValue0().paytable().get(pairSymbol.getValue1()));
        }
        
        if (wonRateForWild != null && wonRateForSymbol != null) {
            wonPair = (wonRateForWild.compareTo(wonRateForSymbol) > 0)?pairWild:pairSymbol;
        }
        else {
            if (wonRateForWild == null && wonRateForSymbol != null) {
                wonPair = pairSymbol;
            } else if (wonRateForWild != null && wonRateForSymbol == null) {
                wonPair = pairWild;
            }
        }
        if (wonPair != null) {
            bettingLine.setWon();
            float multiple = 1;
            if (calculatePayoutArgs.multiplier() > 1) {
                multiple = (isWild(rowMatchBetLine, wonPair.getValue1()))? calculatePayoutArgs.multiplier():1;
            }
            amountLine = bettingLine.betMoney().multiply(wonPair.getValue0().paytable().get(wonPair.getValue1())).multiply(multiple);
            if (amountLine.isGreaterThan(Money.ZERO)) {
//                payLineList.add(( bettingLine.line().id() + ";" + (wonPair.getValue1() + 1) + ";" + amountLine+ ";" + wonPair.getValue0().code()));
//                log.info("Win row match bet line:" + GameUtils.printLine(rowMatchBetLine) + "-- Win (" + wonPair.getValue0() + ":"
//                        + (wonPair.getValue1() + 1) + ", line:"+bettingLine.line().id()+")");
                StringBuffer plbuf = new StringBuffer();
                plbuf
                    .append(bettingLine.line().id()).append(";")
                    .append((wonPair.getValue1() + 1)).append(";")
                    .append(amountLine).append(";")
                    .append(wonPair.getValue0().code());
                payLineList.add(plbuf.toString());
                
                LogMessage.LogMessageBuilder logBuilder = LogMessage.builder();
                logBuilder.cmdId(basePlaySession.commandId())
                    .actorId(basePlaySession.userId())
                    .serviceId(basePlaySession.serviceId())
                    .psId(basePlaySession.uuid())
                    .stateName("Lines")
                    .stepName("updatePlaySession " + basePlaySession.state()).owner(LogMessage.OWNER_GAME)
                    .message("Line = " + GameUtils.printLine(rowMatchBetLine) + " - " + plbuf.toString())
                    .timeExe(0);
                LogsUtils.writeLogDebug(logBuilder.build()); 
                
            }
        }
        return amountLine;
    }

    private boolean isWild(List<Symbol> rowMatchBetLine, int indexPayTable) {
        boolean found = false;
        int i = 0;
        while(i < indexPayTable+1 && found ==false) {
            if (rowMatchBetLine.get(i).type() == SymbolType.WILD) {
                found = true;
            }
            i ++;
        }
        return found;
    }
    
//    @Override
//    public PlaySession updatePlaySession(PayoutAmount payoutAmount, PlaySession playSession) {
//        PlaySession.PlaySessionBuilder builder = playSession.toBuilder();
//        if (payoutAmount.wonAmount().isGreaterThan(Money.ZERO)) {
//
//            if (playSession.state() == GameState.NORMAL_GAME) {
//                builder.addNormalGameWinAmount(payoutAmount.wonAmount());
//
//                List<String> existedData = builder.build().normalGamePayLines();
//                if (existedData != null) {
//                    existedData.addAll(payoutAmount.payLineList());
//                } else {
//                    existedData = payoutAmount.payLineList();
//                }
//                builder.normalGamePayLines(existedData);
//            } else if (playSession.state() == GameState.FREE_GAME){
//                builder.addFreeGameWinAmount(payoutAmount.wonAmount());
//
//                List<String> existedData = builder.build().freeGamePayLines();
//                if (existedData != null) {
//                    existedData.addAll(payoutAmount.payLineList());
//                } else {
//                    existedData = payoutAmount.payLineList();
//                }
//                builder.freeGamePayLines(existedData);
//            } else {
//                log.error("Money can't update in this state="+playSession.state());
//            }
//        }
//        return builder.build();
//    }
}
