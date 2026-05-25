package com.io.begstd.dice.command;

import com.io.begstd.dice.common.DiceGameConstant;
import com.io.begstd.dice.common.DiceGameError;
import com.io.begstd.dice.exception.DiceGameException;
import com.io.begstd.dice.model.app.BasePlaySession;
import com.io.begstd.dice.model.config.IDiceMachineConfig;
import com.io.begstd.dice.model.gamerule.DenominationLevel;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Transform data from configuration file to BetPerLine and BettingLine
 *
 */
@Data
@Accessors(fluent = true)
public class SpinCmd {

    private String totalBetId;
    private String currency = DiceGameConstant.CURRENCY_DEFAULT;
    private List<Integer> lineIds = new ArrayList<>();
    private String lang;

    public DenominationLevel toBetPerLineModel(IDiceMachineConfig diceMachineConfig, BasePlaySession basePlaySession) throws DiceGameException {
        if (this.totalBetId != null && !this.totalBetId.equals("")) {
            char[] arrayBet = totalBetId.toCharArray();
            return diceMachineConfig.getDenominationLevelsForBet(currency).stream()
                .filter(denominationLevel -> denominationLevel.id().equalsIgnoreCase(String.valueOf(arrayBet[0])+"0") && !denominationLevel.env().isEmpty()).findFirst()
                .orElseThrow(() -> new DiceGameException(DiceGameError.INVALID_TOTAL_BET, basePlaySession.userId(), basePlaySession.userType(), basePlaySession.commandId(), String.valueOf(totalBetId)));
        }
        throw new DiceGameException(DiceGameError.INVALID_TOTAL_BET, basePlaySession.userId(), basePlaySession.userType(), basePlaySession.commandId(), String.valueOf(totalBetId));
    }

    public ExtraBetLevelCmd toExtraBet(IDiceMachineConfig diceMachineConfig, BasePlaySession basePlaySession) throws DiceGameException {
        ExtraBetLevelCmd extraBet = null;
        if (this.totalBetId != null && !this.totalBetId.equals("")) {
            int extraBetId = -1;
            try {
                char[] arrayId = totalBetId.toCharArray();
                extraBetId = Integer.parseInt(String.valueOf(arrayId[1]));
            } catch (NumberFormatException ex) {
                throw new DiceGameException(DiceGameError.INVALID_TOTAL_BET, basePlaySession.userId(), basePlaySession.userType(), basePlaySession.commandId());
            }
            extraBet = diceMachineConfig.getExtraBetLevelById(String.valueOf(extraBetId));
            if (extraBet == null && !CollectionUtils.isEmpty(diceMachineConfig.extraBetLevels())) {
                throw new DiceGameException(DiceGameError.INVALID_TOTAL_BET, basePlaySession.userId(), basePlaySession.userType(), basePlaySession.commandId());
            }
            return extraBet;
            
        }
        throw new DiceGameException(DiceGameError.INVALID_TOTAL_BET, basePlaySession.userId(), basePlaySession.userType(), basePlaySession.commandId(), String.valueOf(totalBetId));
    }
}
