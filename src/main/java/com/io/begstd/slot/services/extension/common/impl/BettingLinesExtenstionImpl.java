package com.io.begstd.slot.services.extension.common.impl;

import com.io.begstd.slot.command.SpinCmd;
import com.io.begstd.slot.exception.SlotGameException;
import com.io.begstd.slot.model.app.BasePlaySession;
import com.io.begstd.slot.model.config.ISlotMachineConfig;
import com.io.begstd.slot.model.gamerule.BettingLine;
import com.io.begstd.slot.model.gamerule.DenominationLevel;
import com.io.begstd.slot.services.extension.common.BettingLinesExtension;

import java.util.List;

public class BettingLinesExtenstionImpl implements BettingLinesExtension {

    @Override
    public List<BettingLine> getBettingLine(ISlotMachineConfig slotMachineConfig, DenominationLevel denominationLevel, SpinCmd cmd, Object... args) {
        return cmd.toBettingLine(slotMachineConfig, denominationLevel);
    }

    @Override
    public DenominationLevel toBetPerLineModel(ISlotMachineConfig slotMachineConfig, SpinCmd cmd, BasePlaySession basePlaySession, Object... args) throws SlotGameException {
        return cmd.toBetPerLineModel(slotMachineConfig, basePlaySession);
    }
}
