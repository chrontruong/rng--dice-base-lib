package com.io.begstd.slot.machine.wonrule.impl;

import com.io.begstd.log.LogMessage;
import com.io.begstd.log.LogsUtils;
import com.io.begstd.slot.machine.wonrule.WonRuleExtension;
import com.io.begstd.slot.model.app.BasePlaySession;
import com.io.begstd.slot.model.app.GameState;
import com.io.begstd.slot.model.args.CalculatePayoutArgs;
import com.io.begstd.slot.model.domain.Money;
import com.io.begstd.slot.model.gamerule.Symbol;
import com.io.begstd.slot.model.gamerule.SymbolType;
import org.javatuples.Pair;
import org.javatuples.Triplet;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;

/**
 * Multi plier for symbol
 * */
@Component("allWayToWinMultiplerAmount")
public class AllWayToWinMultiplierAmount implements WonRuleExtension {
    public static final String ALLWAYTOWIN_MULTIPLIER_AMOUNT= "allWayToWinMultiplerAmount";

    @Override
    public String getName() {
        return ALLWAYTOWIN_MULTIPLIER_AMOUNT;
    }

    @Override
    public BasePlaySession calculatorPayout(CalculatePayoutArgs calculatePayoutArgs, BasePlaySession basePlaySession) {
        List<String> payLineList = new ArrayList<String>();
        Money totalWon = this.calculateWonBy(calculatePayoutArgs, payLineList, basePlaySession);
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
                    .stateName("allWayToWinMultiplerAmount")
                    .stepName("updatePlaySession " + basePlaySession.state()).owner(LogMessage.OWNER_GAME)
                    .message("Money can't update in this state " + basePlaySession.state())
                    .timeExe(0);
                LogsUtils.writeLogError(logBuilder.build());
            }
        }
        return builder.build();
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
    
    /*
     * return paytable amount
     */
    private Money calculateWonBy(CalculatePayoutArgs calculatePayoutArgs, List<String> payLineList, BasePlaySession basePlaySession) {

        Money amountTotal = Money.ZERO;
        List<Triplet<Symbol,Integer, Integer>> pairList = calculateWinLineInCombination(calculatePayoutArgs);
        for (Triplet<Symbol,Integer, Integer> pairItem: pairList) {
            Money amountLine = Money.ZERO;
            if (Objects.nonNull(pairItem.getValue0())) {
                if (pairItem.getValue1() > 0) {
                    BigDecimal wonRate = new BigDecimal(pairItem.getValue0().paytable().get(pairItem.getValue2()));
                    if (wonRate.compareTo(BigDecimal.ZERO) > 0) {
                        
                        amountLine = Money.of(pairItem.getValue1()).multiply(wonRate).multiply(calculatePayoutArgs.denomLevel().amount());
                        amountLine = amountLine.multiply(calculatePayoutArgs.multiplier());
                        // payLineList.add((pairItem.getValue0().code() + ";" + amountLine));
                        // Symbol ; Amount; win reel count ; combination count;paytable
                        StringBuffer dataBuffer = new StringBuffer();
                        dataBuffer.append(pairItem.getValue0().code())
                        .append(";").append(amountLine)
                        .append(";").append(pairItem.getValue2()+1) 
                        .append(";").append(pairItem.getValue1())
                        .append(";").append(wonRate);
                        
                        payLineList.add(dataBuffer.toString());
//                        payLineList.add((pairItem.getValue0().code() + ";" + amountLine+";"+(pairItem.getValue2()+1) 
//                                +";"+pairItem.getValue1() +";"+ wonRate));
                        
//                        log.info("Amount :" + amountLine + "-- Win (" + pairItem.getValue0() + ":"
//                                + (pairItem.getValue1()) + ")" +" multiplierValue:"+calculatePayoutArgs.multiplier());
                        
                        LogMessage.LogMessageBuilder logBuilder = LogMessage.builder();
                        logBuilder.cmdId(basePlaySession.commandId())
                            .actorId(basePlaySession.userId())
                            .serviceId(basePlaySession.serviceId())
                            .psId(basePlaySession.uuid())
                            .stateName("AllWayToWinMultiplier")
                            .stepName("calculateWonBy - state " + basePlaySession.state()).owner(LogMessage.OWNER_GAME)
                            .message(dataBuffer.toString() + "; multiplierValue:" + calculatePayoutArgs.multiplier())
                            .timeExe(0);
                        LogsUtils.writeLogDebug(logBuilder.build());
                        
                    }
                }
            }
            amountTotal = amountTotal.add(amountLine);
        }
        return amountTotal;
    }
    //---------------------------------------------------------------
    private List<Triplet<Symbol,Integer, Integer>> calculateWinLineInCombination(CalculatePayoutArgs calculatePayoutArgs) {
        
        List<Triplet<Symbol,Integer, Integer>> pairLineResult = new ArrayList<Triplet<Symbol,Integer, Integer>>();
        
        Symbol[][] viewMatrix = calculatePayoutArgs.matrixScreen().horizontalMatrix();
      //check range to get symbol to calculate
        int reelMaxRangeWithWild = 1;
        int foundWildInReel = 0;
        for (int col = 0; col < calculatePayoutArgs.matrixScreen().reelSize(); col++) {
            foundWildInReel = 0;
            for (int row = 0; row < calculatePayoutArgs.matrixScreen().rowSize(); row++) {
                if (SymbolType.WILD == viewMatrix[row][col].type()) {
                    reelMaxRangeWithWild++;
                    foundWildInReel = 1;
                    break;
                }
            }
            if (foundWildInReel == 0) {
                break;
            }
        }
        
        List<Symbol> allSymbols = new ArrayList<Symbol>();
        Symbol wildSymbol = null;
        for (int row = 0; row < calculatePayoutArgs.matrixScreen().rowSize(); row++) {
            for (int col = 0; col < calculatePayoutArgs.matrixScreen().reelSize();col++) {
                if (col < reelMaxRangeWithWild) { 
                    if (viewMatrix[row][col] != null) {
                        allSymbols.add(viewMatrix[row][col]);
                    }
                }
                if (SymbolType.WILD == viewMatrix[row][col].type()) {
                    wildSymbol = viewMatrix[row][col];
                }
            }
        }
        
        Set<Symbol> uniqueSet = new HashSet<Symbol>(allSymbols);
        
        // calculate score in matrix
        Iterator<Symbol> itr = uniqueSet.iterator();
        while (itr.hasNext()) {
            Symbol countSymbol = itr.next();
            if (countSymbol.type() == SymbolType.SYMBOL) {
                List<Integer> resultReel = new ArrayList<Integer>();
                //calculate for symbol in all column
                
                for (int col = 0; col < calculatePayoutArgs.matrixScreen().reelSize(); col++) {
                    List<Symbol> reelTemp = new ArrayList<Symbol>();
                    for (int i = 0; i < calculatePayoutArgs.matrixScreen().rowSize(); i++) {
                        reelTemp.add(viewMatrix[i][col]);
                    }
                    int frequencySymbol = Collections.frequency(reelTemp, countSymbol);
                    int frequenceWild = 0;
                    if (wildSymbol != null) {
                        frequenceWild = Collections.frequency(reelTemp, wildSymbol);
                    }
                    resultReel.add(frequencySymbol + frequenceWild);
                }
                //calculate score for symbol
                Pair<Integer, Integer> pairPos = null;
                if (calculatePayoutArgs.config().isWinFullLine()) {
                    pairPos = getValueForFullLine(resultReel, calculatePayoutArgs.matrixScreen().reelSize());
                    
                } else {
                    //win a part of line
                    pairPos = getWinPositionForPart(resultReel);
                }
//                log.info("Count reel : "+resultReel+":" + "-- value: "+ pairPos);
                
                pairLineResult.add(new Triplet<>(countSymbol, pairPos.getValue0(), pairPos.getValue1()));   
            }
        }
        return pairLineResult;
    }

    /**
     * get totalCount and Position in payTable 
     * @param listCount
     * @return
     */
    private Pair<Integer, Integer> getValueForFullLine(List<Integer> listCount, int reelSize) {
        int result = 1;
        int pos = 0;
        for( int i : listCount) {
            result *= i;
        }
        if (result != 0)
            pos = reelSize - 1;
        return new Pair<>(result, pos);
    }
    /**
     * get totalCount and Position in payTable 
     * @param listCount
     * @return
     */
    private Pair<Integer, Integer> getWinPositionForPart(List<Integer> listCount) {
        int totalValue = 1;
        int positionCount = 0;
        for( int i : listCount) {
            if (i != 0) {
                positionCount +=1; 
                totalValue *= i;
            } else {
                break;
            }
        }
        return new Pair<>(totalValue, positionCount>0?positionCount-1:0);
    }
    
}
