package com.io.begstd.slot.services.extension.common;

import com.io.begstd.extension.Extension;
import com.io.begstd.slot.command.ExtraBetLevelCmd;
import com.io.begstd.slot.model.config.ISlotMachineConfig;
import com.io.begstd.slot.model.domain.Money;
import com.io.begstd.slot.model.gamerule.DenominationLevel;

import java.util.Map;

public interface JackpotExtension extends Extension{
    default String getName() {
        return this.getClass().getName();
    }
    Map<String, Money> distributeJackPots(ISlotMachineConfig slotMachineConfigNormal, DenominationLevel denominationLevel,
                                          Money totalBet, ExtraBetLevelCmd extrabet);
}
