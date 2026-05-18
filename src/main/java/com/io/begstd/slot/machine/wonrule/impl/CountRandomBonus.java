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
import com.io.begstd.slot.utils.MatrixUtil;
import org.javatuples.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Component("countRandomBonus")
public class CountRandomBonus implements WonRuleExtension {
    public static final String COUNT_RANDOM_BONUS = "countRandomBonus";
    @Autowired
    private Environment environment;
    
    @Override
    public String getName() {
        return COUNT_RANDOM_BONUS;
    }

    
    @Override
    public BasePlaySession calculatorPayout(CalculatePayoutArgs calculatePayoutArgs, BasePlaySession basePlaySession) {
        List<String> payLineList = new ArrayList<String>();
        Money totalWon = calculateWinAmountByBonus(calculatePayoutArgs, payLineList, basePlaySession);
        if(totalWon.isGreaterThan(Money.ZERO) && Arrays.asList(environment.getActiveProfiles()).contains("rtpdebug")) {
            MatrixUtil.logMatrixRTP(calculatePayoutArgs.matrixScreen().horizontalMatrixDataCell(), ("countRandomBonus--"+ basePlaySession.uuid()
            +" ---state:"+ basePlaySession.state()+" ---totalWon:"+totalWon),
                    (basePlaySession.state() == GameState.NORMAL_GAME? basePlaySession.normalGameTableFormat():  basePlaySession.freeGameTableFormat()));
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
                builder.addFreeGameWinAmountWithLatest(wonAmount);

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
                    .stateName("CountRandomBonus")
                    .stepName("updatePlaySession " + basePlaySession.state()).owner(LogMessage.OWNER_GAME)
                    .message("Money can't update in this state " + basePlaySession.state())
                    .timeExe(0);
                LogsUtils.writeLogError(logBuilder.build());
            }
        }
        return builder.build();
    }
    
    public Money calculateWinAmountByBonus(CalculatePayoutArgs calculatePayoutArgs, List<String> payLineList, BasePlaySession basePlaySession) {
        // Check scatter won
        Money bonusWonMoney = Money.ZERO;
        Pair<Symbol, Integer> bonusAndCount = calculatePayoutArgs.matrixScreen().countSymbolByType(SymbolType.BONUS);
        if (Objects.nonNull(bonusAndCount.getValue0()) && 
            Objects.nonNull(bonusAndCount.getValue0().paytable())) {
            BigDecimal wonRate = new BigDecimal(
                    bonusAndCount.getValue0().paytable().get(bonusAndCount.getValue1() - 1));
            //win bonus = demon * paytable of bonus
            bonusWonMoney = calculatePayoutArgs.denomLevel().amount().multiply(wonRate);
            
            LogMessage.LogMessageBuilder logBuilder = LogMessage.builder();
            logBuilder.cmdId(basePlaySession.commandId())
                .actorId(basePlaySession.userId())
                .serviceId(basePlaySession.serviceId())
                .psId(basePlaySession.uuid())
                .stateName("CountRandomBonus")
                .stepName("calculateWinAmountByBonus").owner(LogMessage.OWNER_GAME)
                .message("Win random bonus -- Win (" + bonusAndCount.getValue0() + ":"
                        + (bonusAndCount.getValue1()) + ") = "+ bonusWonMoney.value().doubleValue())
                .timeExe(0);
            LogsUtils.writeLogDebug(logBuilder.build());
            
        }
        return bonusWonMoney;
    }
}
