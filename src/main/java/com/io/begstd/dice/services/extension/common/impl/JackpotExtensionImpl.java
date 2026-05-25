package com.io.begstd.dice.services.extension.common.impl;

import com.io.begstd.dice.model.config.IDiceMachineConfig;
import com.io.begstd.dice.model.domain.Money;
import com.io.begstd.dice.model.gamerule.DenominationLevel;
import com.io.begstd.dice.services.extension.common.JackpotExtension;
import com.io.begstd.dice.utils.GameUtils;
import com.io.begstd.dice.command.ExtraBetLevelCmd;

import java.util.Map;

public class JackpotExtensionImpl implements JackpotExtension {
    
    @Override
    public Map<String, Money> distributeJackPots(IDiceMachineConfig slotMachineConfig, DenominationLevel denominationLevel,
                                                 Money totalBet, ExtraBetLevelCmd extrabet) {
        return GameUtils.distributeJackPots(slotMachineConfig, denominationLevel, totalBet);
    }
}
