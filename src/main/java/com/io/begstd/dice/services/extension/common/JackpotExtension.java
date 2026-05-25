package com.io.begstd.dice.services.extension.common;

import com.io.begstd.extension.Extension;
import com.io.begstd.dice.model.config.IDiceMachineConfig;
import com.io.begstd.dice.model.domain.Money;
import com.io.begstd.dice.model.gamerule.DenominationLevel;
import com.io.begstd.dice.command.ExtraBetLevelCmd;

import java.util.Map;

public interface JackpotExtension extends Extension{
    default String getName() {
        return this.getClass().getName();
    }
    Map<String, Money> distributeJackPots(IDiceMachineConfig diceMachineConfigNormal, DenominationLevel denominationLevel,
                                          Money totalBet, ExtraBetLevelCmd extrabet);
}
