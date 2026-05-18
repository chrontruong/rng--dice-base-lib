package com.io.begstd.slot.rules.matrix.pregenerate;

import com.io.begstd.slot.model.app.BasePlaySession;
import com.io.begstd.slot.model.config.ISlotMachineConfig;
import com.io.begstd.slot.model.gamerule.DataCell;
import com.io.begstd.slot.model.gamerule.Symbol;

public interface IPreGenerateMatrixRule {
    /**
     * 
     * @param basePlaySession
     * @param config
     * @param verticalMatrix: cloned matrix, set Symbol directly on this matrix, don't need to be cloned when implement
     * @return
     */
    public DataCell<Symbol>[][] preGenerateMatrix(BasePlaySession basePlaySession, ISlotMachineConfig config, DataCell<Symbol>[][] verticalMatrix);
}
