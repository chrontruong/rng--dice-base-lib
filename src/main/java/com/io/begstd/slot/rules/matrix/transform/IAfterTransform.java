package com.io.begstd.slot.rules.matrix.transform;

import com.io.begstd.slot.model.app.BasePlaySession;
import com.io.begstd.slot.model.config.ISlotMachineConfig;
import com.io.begstd.slot.model.gamerule.DataCell;
import com.io.begstd.slot.model.gamerule.Symbol;

import java.util.List;

public interface IAfterTransform {
    BasePlaySession doAfterTransform(BasePlaySession currentSession, List<DataCell<Symbol>[][]> verticalListTransformedMatrix, ISlotMachineConfig config);
}
