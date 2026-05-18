package com.io.begstd.slot.services.extension.common.impl;

import com.io.begstd.slot.command.ExtraBetLevelCmd;
import com.io.begstd.slot.model.app.BasePlaySession;
import com.io.begstd.slot.model.config.ISlotMachineConfig;
import com.io.begstd.slot.model.domain.Money;
import com.io.begstd.slot.model.gamerule.DenominationLevel;
import com.io.begstd.slot.services.extension.common.DenominationExtension;
import com.io.begstd.slot.utils.GameUtils;

public class DenominationExtensionImpl implements DenominationExtension {

    @Override
    public Money convertToTotalBet(BasePlaySession basePlaySession, ISlotMachineConfig slotMachineConfig) {
        Money bettingTotal = null;
        if (slotMachineConfig.payLines().isEmpty()) {
            // always to win
            DenominationLevel betDemon = basePlaySession.betDenom();
            ExtraBetLevelCmd extraBet = basePlaySession.extraDenom();
            if ((extraBet != null) && (extraBet.amount() != null) && (extraBet.amount().floatValue() > 0.0)) {
                bettingTotal = betDemon.amount().multiply(slotMachineConfig.totalCredit()).multiply(extraBet.amount());
            } else {
                bettingTotal = betDemon.amount().multiply(slotMachineConfig.totalCredit());
            }
        } else {
            bettingTotal = GameUtils.calculateBettingTotal(basePlaySession.bettingLines());
        }
        return bettingTotal;
    }

}
