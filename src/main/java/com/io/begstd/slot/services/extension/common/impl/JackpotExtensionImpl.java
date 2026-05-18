package com.io.begstd.slot.services.extension.common.impl;

import com.io.begstd.slot.command.ExtraBetLevelCmd;
import com.io.begstd.slot.model.config.ISlotMachineConfig;
import com.io.begstd.slot.model.domain.Money;
import com.io.begstd.slot.model.gamerule.DenominationLevel;
import com.io.begstd.slot.services.extension.common.JackpotExtension;
import com.io.begstd.slot.utils.GameUtils;

import java.util.Map;

public class JackpotExtensionImpl implements JackpotExtension {
    
    @Override
    public Map<String, Money> distributeJackPots(ISlotMachineConfig slotMachineConfig, DenominationLevel denominationLevel,
                                                 Money totalBet, ExtraBetLevelCmd extrabet) {
        return GameUtils.distributeJackPots(slotMachineConfig, denominationLevel, totalBet);
    }
}
