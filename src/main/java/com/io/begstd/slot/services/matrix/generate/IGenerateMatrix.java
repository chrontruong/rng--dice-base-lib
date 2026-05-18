package com.io.begstd.slot.services.matrix.generate;

import com.io.begstd.slot.model.app.BasePlaySession;
import com.io.begstd.slot.model.config.ISlotMachineConfig;
import com.io.begstd.slot.model.gamerule.DataCell;
import com.io.begstd.slot.model.gamerule.Symbol;
import org.javatuples.Pair;

import java.util.List;

public interface IGenerateMatrix {
    
    /**
     * 
     * @param currentBasePlaySession
     * @return:
     * - DataCell[][] : generated vertical matrix.
     * - List<Integer>: table format {reel1Size, reel2Size, reel3Size,...}
     */
    Pair<DataCell<Symbol>[][], List<Integer>> generateMatrix(BasePlaySession currentBasePlaySession, ISlotMachineConfig config);
}
