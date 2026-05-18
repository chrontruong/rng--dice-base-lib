package com.io.begstd.slot.command;

import com.io.begstd.slot.common.SlotGameConstant;
import com.io.begstd.slot.common.SlotGameError;
import com.io.begstd.slot.exception.SlotGameException;
import com.io.begstd.slot.model.app.BasePlaySession;
import com.io.begstd.slot.model.config.ISlotMachineConfig;
import com.io.begstd.slot.model.gamerule.BettingLine;
import com.io.begstd.slot.model.gamerule.DenominationLevel;
import com.io.begstd.slot.model.gamerule.PayLine;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Transform data from configuration file to BetPerLine and BettingLine
 *
 */
@Data
@Accessors(fluent = true)
public class SpinCmd {

    private String totalBetId;//"12"
    private String currency = SlotGameConstant.CURRENCY_DEFAULT;
    private List<Integer> lineIds = new ArrayList<>();
    private String lang;

    public DenominationLevel toBetPerLineModel(ISlotMachineConfig slotMachineConfig, BasePlaySession basePlaySession) throws SlotGameException {
        if (this.totalBetId != null && !this.totalBetId.equals("")) {
            char[] arrayBet = totalBetId.toCharArray();
            return slotMachineConfig.getDenominationLevelsForBet(currency).stream()
                .filter(denominationLevel -> denominationLevel.id().equalsIgnoreCase(String.valueOf(arrayBet[0])+"0") && !denominationLevel.env().isEmpty()).findFirst()
                .orElseThrow(() -> new SlotGameException(SlotGameError.INVALID_TOTAL_BET, basePlaySession.userId(), basePlaySession.userType(), basePlaySession.commandId(), String.valueOf(totalBetId)));
        }
        throw new SlotGameException(SlotGameError.INVALID_TOTAL_BET, basePlaySession.userId(), basePlaySession.userType(), basePlaySession.commandId(), String.valueOf(totalBetId));
    }

    public ExtraBetLevelCmd toExtraBet(ISlotMachineConfig slotMachineConfig, BasePlaySession basePlaySession) throws SlotGameException {
        ExtraBetLevelCmd extraBet = null;
        if (this.totalBetId != null && !this.totalBetId.equals("")) {
            int extraBetId = -1;
            try {
                char[] arrayId = totalBetId.toCharArray();
                extraBetId = Integer.parseInt(String.valueOf(arrayId[1]));
            } catch (NumberFormatException ex) {
                throw new SlotGameException(SlotGameError.INVALID_TOTAL_BET, basePlaySession.userId(), basePlaySession.userType(), basePlaySession.commandId());
            }
            extraBet = slotMachineConfig.getExtraBetLevelById(String.valueOf(extraBetId));
            if (extraBet == null && !CollectionUtils.isEmpty(slotMachineConfig.extraBetLevels())) {
                throw new SlotGameException(SlotGameError.INVALID_TOTAL_BET, basePlaySession.userId(), basePlaySession.userType(), basePlaySession.commandId());
            }
            return extraBet;
            
        }
        throw new SlotGameException(SlotGameError.INVALID_TOTAL_BET, basePlaySession.userId(), basePlaySession.userType(), basePlaySession.commandId(), String.valueOf(totalBetId));
    }
    
    public List<BettingLine> toBettingLine(ISlotMachineConfig slotMachineConfig, DenominationLevel denominationLevel) {
        Map<Integer, PayLine> payLineMap = slotMachineConfig.payLines().stream()
                .collect(Collectors.toMap(PayLine::id, e -> e));

        float creditLine = (float) slotMachineConfig.totalCredit()/slotMachineConfig.payLines().size();
        
        List<BettingLine> betPerLineList = null;
        if (this.lineIds().isEmpty()) {
            // if client send empty -> bet all paylines and transform to
            // BettingLine
            betPerLineList = slotMachineConfig.payLines().stream()
                    .map(payLine -> new BettingLine(payLine, denominationLevel.amount().multiply(creditLine))).collect(Collectors.toList());
        } else {
            // else transform to BettingLine
            betPerLineList = this.lineIds().stream().map(payLineMap::get)
                    .map(payLine -> new BettingLine(payLine, denominationLevel.amount().multiply(creditLine))).collect(Collectors.toList());
        }

        return betPerLineList;
    }
}
