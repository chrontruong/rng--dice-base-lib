package com.io.begstd.slot.rules.matrix.pregenerate;

import com.io.begstd.slot.model.app.BasePlaySession;
import com.io.begstd.slot.model.config.ISlotMachineConfig;

import java.util.List;

public interface IDefineMatrixFormatRule {
    /**
     * 
     * @param config
     * @param basePlaySession
     * @return: matrix format, ex: {3,4,5,4,3}
     */
    public List<Integer> defineMatrixFormat(ISlotMachineConfig config, BasePlaySession basePlaySession);
}
