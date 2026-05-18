package com.io.begstd.slot.rules.matrix.pregenerate;

import com.io.begstd.slot.model.app.BasePlaySession;
import com.io.begstd.slot.model.config.ISlotMachineConfig;
import com.io.begstd.slot.model.gamerule.DataCell;
import com.io.begstd.slot.model.gamerule.Symbol;

public interface IAfterGenerate {
    BasePlaySession doAfterGenerate(BasePlaySession currentSession, DataCell<Symbol>[][] verticalMatrix, ISlotMachineConfig config);
}
