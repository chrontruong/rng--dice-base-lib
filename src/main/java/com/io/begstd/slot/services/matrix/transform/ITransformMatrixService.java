package com.io.begstd.slot.services.matrix.transform;

import com.io.begstd.slot.model.app.BasePlaySession;
import com.io.begstd.slot.model.config.ISlotMachineConfig;
import com.io.begstd.slot.model.gamerule.DataCell;
import com.io.begstd.slot.model.gamerule.Symbol;

import java.util.List;

public interface ITransformMatrixService {
    
    /**
     * 
     * @param verticalMatrix: Vertical matrix
     * @return List generated vertical matrix
     */
    public List<DataCell<Symbol>[][]> transformMatrix(DataCell<Symbol>[][] verticalMatrix, BasePlaySession basePlaySession, ISlotMachineConfig config);
}
