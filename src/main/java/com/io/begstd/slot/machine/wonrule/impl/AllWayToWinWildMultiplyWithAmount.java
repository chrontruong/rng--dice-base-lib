package com.io.begstd.slot.machine.wonrule.impl;

import com.io.begstd.slot.machine.wonrule.WonRuleExtension;
import com.io.begstd.slot.model.app.BasePlaySession;
import com.io.begstd.slot.model.app.GameState;
import com.io.begstd.slot.model.args.CalculatePayoutArgs;
import com.io.begstd.slot.model.domain.Money;
import com.io.begstd.slot.model.gamerule.Symbol;
import com.io.begstd.slot.model.gamerule.SymbolType;
import com.io.begstd.slot.utils.MatrixUtil;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.javatuples.Pair;
import org.javatuples.Triplet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.*;
@Slf4j
@Component
public class AllWayToWinWildMultiplyWithAmount implements WonRuleExtension {
    @Autowired
    private Environment environment;
    
    @Override
    public String getName() {
        return "allWayToWinWildMultiplyWithAmount";
    }

    @Override
    public BasePlaySession calculatorPayout(CalculatePayoutArgs calculatePayoutArgs, BasePlaySession basePlaySession) {
        List<String> payLineList = new ArrayList<String>();
        List<CombinationLine> listComnbinationLine = calculateCombinationLine(calculatePayoutArgs);
        
        Money totalWon = Money.ZERO;
        Money amountCombine = Money.ZERO;
        for (CombinationLine combinationLine : listComnbinationLine) {
            amountCombine = Money.ZERO;
            if (combinationLine.numberCombination() > 0) {
                int wonRate = combinationLine.winSymbol().paytable().get(combinationLine.numberWinReel()-1);
                if (wonRate > 0) {
                    amountCombine = Money.of(combinationLine.numberCombination()).multiply(wonRate). multiply(calculatePayoutArgs.denomLevel().amount());
                    float multiplier = 1;
                    if (combinationLine.hasWild()) {
                        multiplier = calculatePayoutArgs.multiplier();
                    }
                    amountCombine = amountCombine.multiply(multiplier);
                    payLineList.add(combinationLine.winSymbol().code() + ";" + amountCombine +
                            ";" + combinationLine.numberWinReel() + ";" + combinationLine.numberCombination()+
                            ";" + wonRate + ";" + multiplier+ ";"+ (combinationLine.hasWild()?1:0));
                    
                    if(Arrays.asList(environment.getActiveProfiles()).contains("rtpdebug")) {
                        String data = combinationLine.winSymbol().code() + ";" + amountCombine +
                                ";" + combinationLine.numberWinReel() + ";" + combinationLine.numberCombination()+
                                ";" + wonRate + ";" + multiplier+ ";"+ (combinationLine.hasWild()?1:0);
                        log.error("uuid:"+ basePlaySession.uuid() +" --state:"+ basePlaySession.state()+ " --Win:"+data+" ---freeGameRemain:"+ basePlaySession.freeGameRemain());
                    }
                }
                totalWon = totalWon.add(amountCombine);
            }
        }
        if(totalWon.gt(Money.ZERO)) {
            if(Arrays.asList(environment.getActiveProfiles()).contains("rtpdebug")) {
                MatrixUtil.logMatrixRTP(calculatePayoutArgs.matrixScreen().horizontalMatrixDataCell(), ("Matrix CHECK--"+ basePlaySession.uuid() +" ---state:"+ basePlaySession.state()+" ---totalWon:"+totalWon+"---freeGameRemain:"+ basePlaySession.freeGameRemain()),
                    (basePlaySession.state() == GameState.NORMAL_GAME? basePlaySession.normalGameTableFormat():  basePlaySession.freeGameTableFormat()));
            }
        }
        return updatePlaySession(basePlaySession, totalWon, payLineList);
    }
    
    private BasePlaySession updatePlaySession(BasePlaySession basePlaySession, Money wonAmount, List<String> payLineList) {
        BasePlaySession.BasePlaySessionBuilder<?,?> psBuilder = basePlaySession.toBuilder();
        
        if (wonAmount.gt(Money.ZERO)) {
            if (basePlaySession.state() == GameState.NORMAL_GAME) {
                psBuilder.addNormalGameWinAmount(wonAmount);
                psBuilder.addNormalGamePayLines(payLineList);
            }
            
            if (basePlaySession.state() == GameState.FREE_GAME) {
                psBuilder.addFreeGameWinAmountWithLatest(wonAmount);
                psBuilder.addFreeGamePayLines(payLineList);
            }
        }
        
        return psBuilder.build();
    }
    
    private List<CombinationLine> calculateCombinationLine(CalculatePayoutArgs calculatePayoutArgs) {
        List<CombinationLine> listComnbinationLine = new ArrayList<>();
        
        Symbol[][] viewMatrix = calculatePayoutArgs.matrixScreen().horizontalMatrix();
        
        int maxReelSizeHasCombination = 1;
        boolean foundWildInReel = false;
        for (int col = 0; col < calculatePayoutArgs.matrixScreen().reelSize(); col++) {
            foundWildInReel = false;
            for (int row = 0; row < calculatePayoutArgs.matrixScreen().rowSize(); row++) {
                if (viewMatrix[row][col] != null && SymbolType.WILD == viewMatrix[row][col].type()) {
                    maxReelSizeHasCombination++;
                    foundWildInReel = true;
                    break;
                }
            }
            if (!foundWildInReel) {
                break;
            }
        }
        
        if (maxReelSizeHasCombination > calculatePayoutArgs.matrixScreen().reelSize()) {
            maxReelSizeHasCombination = calculatePayoutArgs.matrixScreen().reelSize();
        }
        
        Set<Symbol> allSymbols = new HashSet<Symbol>();
        for (int row = 0, rowSize = calculatePayoutArgs.matrixScreen().rowSize(); row < rowSize; row++) {
            for (int col = 0; col < maxReelSizeHasCombination; col++) {
                if (viewMatrix[row][col] != null) {
                    allSymbols.add(viewMatrix[row][col]);
                }
            }
        }
        
        for (Symbol countedSymbol : allSymbols) {
            if (countedSymbol.type() == SymbolType.SYMBOL) {
                List<Pair<Integer, Integer>> resultReel = new ArrayList<Pair<Integer, Integer>>(); // Pair {frequencySymbol, frequencyWild}

                //calculate for symbol in all column
                for (int col = 0; col < calculatePayoutArgs.matrixScreen().reelSize(); col++) {
                    List<Symbol> symbolOnReel = new ArrayList<Symbol>();
                    for (int row = 0; row < calculatePayoutArgs.matrixScreen().rowSize(); row++) {
                        if (viewMatrix[row][col] != null) {
                            symbolOnReel.add(viewMatrix[row][col]);
                        }
                    }
                    
                    int frequencySymbol = MatrixUtil.countSymbolByCode(symbolOnReel, countedSymbol.code());
                    int frequencyWild = MatrixUtil.countSymbolByType(symbolOnReel, SymbolType.WILD);
                    
                    resultReel.add(new Pair<Integer, Integer>( frequencySymbol, frequencyWild));
                }
                
                //calculate score for symbol
                Triplet<Integer, Integer, Boolean> symbolCalculatedData = null; // Triplet {numberCombination, numberWinReel, isExistWildOnCombine}
                //win a part of line
                symbolCalculatedData = getWinPosition(resultReel);
                listComnbinationLine.add(new CombinationLine.CombinationLineBuilder()
                                   .winSymbol(countedSymbol)
                                   .numberCombination(symbolCalculatedData.getValue0())
                                   .numberWinReel(symbolCalculatedData.getValue1())
                                   .hasWild(symbolCalculatedData.getValue2())
                                   .build()
                        );
            }
        }
        return listComnbinationLine;
    }
    
    /**
     * get totalCount and Position in payTable
     * @param resultReel: Pair {frequencySymbol, frequencyWild}
     * @return Triplet {numberCombination, numberWinReel, isExistWildOnCombine}
     */
    private Triplet<Integer, Integer, Boolean> getWinPosition(List<Pair<Integer, Integer>> resultReel) {
        int numberCombination = 1;
        int numberWinReel = 0;
        boolean isExistWildOnCombine = false;
        for( Pair<Integer, Integer> pair : resultReel) {
            int countSymbol = pair.getValue0() + pair.getValue1(); // frequencySymbol + frequencyWild
            if (countSymbol != 0) {
                numberWinReel +=1; 
                numberCombination *= countSymbol;
                if (pair.getValue1() > 0) { // frequencyWild
                    isExistWildOnCombine = true;
                }
            } else {
                break;
            }
        }
        return new Triplet<Integer, Integer, Boolean>(numberCombination, numberWinReel, isExistWildOnCombine);
    }

}

@Getter
@Accessors(fluent = true)
@Builder
class CombinationLine {
    private Symbol winSymbol;
    private boolean hasWild;
    private int numberCombination;
    private int numberWinReel;
}
