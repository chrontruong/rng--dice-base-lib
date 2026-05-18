package com.io.begstd.slot.rules.matrix.transform.impl;

import com.io.begstd.slot.model.app.BasePlaySession;
import com.io.begstd.slot.model.config.ISlotMachineConfig;
import com.io.begstd.slot.model.config.TransformMatrixConfig;
import com.io.begstd.slot.model.gamerule.DataCell;
import com.io.begstd.slot.model.gamerule.Symbol;
import com.io.begstd.slot.rules.matrix.transform.ITransformMatrixRule;
import com.io.begstd.slot.utils.MatrixUtil;

public abstract class AbstractTransformMatrixRule implements ITransformMatrixRule {

    @Override
    public final DataCell<Symbol>[][] transformMatrix(DataCell<Symbol>[][] verticalMatrix,
                                                      TransformMatrixConfig config,
                                                      BasePlaySession basePlaySession,
                                                      ISlotMachineConfig slotMachineConfig) {

        verticalMatrix = MatrixUtil.cloneMatrix(verticalMatrix);

        return doTransformMatrix(verticalMatrix, config, basePlaySession, slotMachineConfig);
    }

    /**
     * do the transform on matrix param directly.
     * 
     * @param horizontalMatrix: cloned matrix. this matrix can be replaced item
     *                          directly.
     * @param config            : config property.
     * @return the history of transformed item.
     */
    public abstract DataCell<Symbol>[][] doTransformMatrix(DataCell<Symbol>[][] verticalMatrix,
                                                           TransformMatrixConfig config,
                                                           BasePlaySession basePlaySession,
                                                           ISlotMachineConfig slotMachineConfig);
}
