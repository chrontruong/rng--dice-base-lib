package com.io.begstd.slot.services.extension.common;

import com.io.begstd.extension.Extension;
import com.io.begstd.slot.command.SpinCmd;
import com.io.begstd.slot.model.app.BasePlaySession;
import com.io.begstd.slot.model.config.ISlotMachineConfig;
import com.io.begstd.slot.model.gamerule.BettingLine;
import com.io.begstd.slot.model.gamerule.DenominationLevel;

import java.util.List;

public interface BettingLinesExtension extends Extension {
    default String getName() {
        return this.getClass().getName();
    }
    List<BettingLine> getBettingLine(ISlotMachineConfig slotMachineConfig, DenominationLevel denominationLevel, SpinCmd cmd, Object... args);
    DenominationLevel toBetPerLineModel(ISlotMachineConfig slotMachineConfig, SpinCmd cmd, BasePlaySession basePlaySession, Object... args);
}
