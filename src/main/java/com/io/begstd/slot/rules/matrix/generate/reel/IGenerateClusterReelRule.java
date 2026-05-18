package com.io.begstd.slot.rules.matrix.generate.reel;

import com.io.begstd.slot.model.gamerule.DataCell;
import com.io.begstd.slot.model.gamerule.Symbol;

import java.util.List;

public interface IGenerateClusterReelRule extends IGenerateReelRule{

    public DataCell<Symbol>[] generateClusterReel(DataCell<Symbol>[] preReelSymbol, 
            Symbol[] baseReelSymbol, List<Integer> percentConfigForSymbol);
}
