package com.io.begstd.slot.rules.matrix.transform;

import com.io.begstd.slot.model.app.BasePlaySession;
import com.io.begstd.slot.model.config.ISlotMachineConfig;
import com.io.begstd.slot.model.config.TransformMatrixConfig;
import com.io.begstd.slot.model.gamerule.DataCell;
import com.io.begstd.slot.model.gamerule.Symbol;

/**
 * 
 * Note: 
 * - 1. If implement ITransformMatrixRule directly, you must return new reference of transformed matrix. 
 * - 2. Should extend AbstractTransformMatrixRule and override doTransformMatrix, AbstractTransformMatrixRule handle cloned matrix, no addition action require.
 */

public interface ITransformMatrixRule {
    /**
     * Do transform base config.
     * @param verticalMatrix: current matrix
     * @param config : config property
     * @param basePlaySession
     * @return new cloned matrix, if no change, return null;
     */
    public DataCell<Symbol>[][] transformMatrix(DataCell<Symbol>[][] verticalMatrix,
                                                TransformMatrixConfig config,
                                                BasePlaySession basePlaySession,
                                                ISlotMachineConfig slotMachineConfig);
}
