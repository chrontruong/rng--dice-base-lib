package com.io.begstd.dice.services.extension.common.impl;

import com.io.begstd.dice.model.app.BasePlaySession;
import com.io.begstd.dice.model.config.IDiceMachineConfig;
import com.io.begstd.dice.model.domain.Money;
import com.io.begstd.dice.services.extension.common.DenominationExtension;
import com.io.begstd.dice.command.ExtraBetLevelCmd;
import com.io.begstd.dice.model.gamerule.DenominationLevel;

public class DenominationExtensionImpl implements DenominationExtension {

    @Override
    public Money convertToTotalBet(BasePlaySession basePlaySession, IDiceMachineConfig diceMachineConfig) {
        Money bettingTotal = null;
        DenominationLevel betDemon = basePlaySession.betDenom();
        ExtraBetLevelCmd extraBet = basePlaySession.extraDenom();

        if (extraBet != null && extraBet.amount() != null && extraBet.amount().floatValue() > 0.0) {
            bettingTotal = betDemon.amount().multiply(diceMachineConfig.totalCredit()).multiply(extraBet.amount());
        } else {
            bettingTotal = betDemon.amount().multiply(diceMachineConfig.totalCredit());
        }
        return bettingTotal;
    }

}
