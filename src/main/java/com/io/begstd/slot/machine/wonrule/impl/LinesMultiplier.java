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
import com.io.begstd.slot.utils.MatrixUtil;
import lombok.extern.slf4j.Slf4j;
import org.javatuples.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
/**
 * Calculate award with comparing WILD paytable and symbol win paytable in the same payline,
 * after that won paytable will multiply multipler param
 *
 */
@Slf4j
@Component("lineMultipler")
public class LinesMultiplier implements WonRuleExtension {

    public static final String LINE_MULTIPLIER = "lineMultipler";
    @Autowired
    private Environment environment;
    
    @Override
    public String getName() {
        return LINE_MULTIPLIER;
    }

    @Override
    public BasePlaySession calculatorPayout(CalculatePayoutArgs calculatePayoutArgs, BasePlaySession basePlaySession) {
        List<String> payLineList = new ArrayList<String>();

        
        Money totalWon = calculatePayoutArgs.bettingLines().stream().map(bettingLine -> calculateWonBy(bettingLine, calculatePayoutArgs, payLineList, basePlaySession)).reduce(Money::add).get();
        if (totalWon.isGreaterThan(Money.ZERO)) {
            if(Arrays.asList(environment.getActiveProfiles()).contains("rtpdebug")) {
                MatrixUtil.logMatrixRTP(calculatePayoutArgs.matrixScreen().horizontalMatrixDataCell(), "lineMultipler--"+ basePlaySession.uuid(),
                        (basePlaySession.state() == GameState.NORMAL_GAME? basePlaySession.normalGameTableFormat():  basePlaySession.freeGameTableFormat()));
            }
        }
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
//              log.error("Money can't update in this state="+playSession.state());
                LogMessage.LogMessageBuilder logBuilder = LogMessage.builder();
                logBuilder.cmdId(basePlaySession.commandId())
                    .actorId(basePlaySession.userId())
                    .serviceId(basePlaySession.serviceId())
                    .psId(basePlaySession.uuid())
                    .stateName("LinesMultiplier")
                    .stepName("updatePlaySession " + basePlaySession.state()).owner(LogMessage.OWNER_GAME)
                    .message("Money can't update in this state " + basePlaySession.state())
                    .timeExe(0);
                LogsUtils.writeLogError(logBuilder.build());
            }
        }
        return builder.build();
    }

    private Money calculateWonBy(BettingLine bettingLine, CalculatePayoutArgs calculatePayoutArgs, List<String> payLineList, BasePlaySession basePlaySession) {

        Money amountLine = Money.ZERO;
        List<Symbol> rowMatchBetLine = calculatePayoutArgs.matrixScreen().findRowByPerLine(bettingLine.line());
        Pair<Symbol, Integer> pair = calculatePayoutArgs.matrixScreen().countSymbolInLine(rowMatchBetLine);

        if (Objects.nonNull(pair.getValue0()) && 
            Objects.nonNull(pair.getValue0().paytable()) &&
            Objects.nonNull(pair.getValue0().paytable().get(pair.getValue1()))) {
            
            BigDecimal wonRate = new BigDecimal(pair.getValue0().paytable().get(pair.getValue1()));

            if (wonRate.compareTo(BigDecimal.ZERO) > 0) {
                bettingLine.setWon();
                float multiple = 1;
                if (calculatePayoutArgs.multiplier() > 1) {
                    multiple = (isWild(rowMatchBetLine, pair.getValue1()))? calculatePayoutArgs.multiplier():1;
                }
                amountLine = bettingLine.betMoney().multiply(wonRate).multiply(multiple);
                
                StringBuffer paylineBuf = new StringBuffer();
                paylineBuf
                    .append(bettingLine.line().id()).append(";")
                    .append((pair.getValue1() + 1)).append(";")
                    .append(amountLine).append(";")
                    .append(pair.getValue0().code());
                payLineList.add(paylineBuf.toString());
                
//                payLineList.add((bettingLine.line().id() + ";" + (pair.getValue1() + 1) + ";" + amountLine+ ";" + pair.getValue0().code()));
                
//                log.info("Win row match bet line:" + GameUtils.printLine(rowMatchBetLine) + "-- Win (" + pair.getValue0() + ":"
//                        + (pair.getValue1() + 1) + ")");
                
                LogMessage.LogMessageBuilder logBuilder = LogMessage.builder();
                logBuilder.cmdId(basePlaySession.commandId())
                    .actorId(basePlaySession.userId())
                    .serviceId(basePlaySession.serviceId())
                    .psId(basePlaySession.uuid())
                    .stateName("LinesMultiplier")
                    .stepName("updatePlaySession " + basePlaySession.state()).owner(LogMessage.OWNER_GAME)
                    .message("Line = " + GameUtils.printLine(rowMatchBetLine) + " - " +paylineBuf.toString() + " multiplier "+ multiple)
                    .timeExe(0);
                LogsUtils.writeLogDebug(logBuilder.build()); 
                
                if(Arrays.asList(environment.getActiveProfiles()).contains("rtpdebug")) {
//                    MatrixUtil.logMatrixRTP(calculatePayoutArgs.matrixScreen().horizontalMatrixDataCell(), "lineMultipler", Arrays.asList(3,3,3,3,3));
                    log.error("Line Id:"+bettingLine.line().id()+"Win row match bet line:" + GameUtils.printLine(rowMatchBetLine) + "-- Win (" + pair.getValue0() + ":"
                            + (pair.getValue1() + 1) + ")" +"--amountLine:"+amountLine+"---multiple:"+multiple);
                }
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
