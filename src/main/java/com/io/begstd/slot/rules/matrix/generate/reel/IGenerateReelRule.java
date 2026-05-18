package com.io.begstd.slot.rules.matrix.generate.reel;

import com.io.begstd.slot.model.gamerule.DataCell;
import com.io.begstd.slot.model.gamerule.Symbol;

public interface IGenerateReelRule {
    public DataCell<Symbol>[] generateReel(DataCell<Symbol>[] preReelSymbol, 
            Symbol[] baseReelSymbol);
}
