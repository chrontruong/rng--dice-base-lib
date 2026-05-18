package com.io.begstd.slot.rules.matrix.generate.reel.impl;

import com.io.begstd.slot.model.gamerule.DataCell;
import com.io.begstd.slot.model.gamerule.Symbol;
import com.io.begstd.slot.rules.matrix.generate.reel.IGenerateReelRule;
import org.springframework.stereotype.Component;

/**
 * 
 *         Generate reel base on base reel. Additional rule: + Only one Scatter
 *         can be exist. + Only one Bonus can be exist.
 *
 */
@Component("randomByBaseReelRule")
public class DefaultRandomReelRule implements IGenerateReelRule {

    @Override
    public DataCell<Symbol>[] generateReel(DataCell<Symbol>[] preReelSymbol, Symbol[] baseReelSymbol) {

        return preReelSymbol;
    }

}
