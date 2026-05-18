package com.io.begstd.slot.rules.matrix.transform.impl;

import com.io.begstd.slot.model.app.BasePlaySession;
import com.io.begstd.slot.model.config.ISlotMachineConfig;
import com.io.begstd.slot.model.config.TransformMatrixConfig;
import com.io.begstd.slot.model.gamerule.DataCell;
import com.io.begstd.slot.model.gamerule.Symbol;
import com.io.begstd.slot.model.gamerule.SymbolType;
import org.springframework.stereotype.Component;

@Component("expandWildOnReelTransformRule")
public class ExpandWildOnReelTransformRule extends AbstractTransformMatrixRule {

    @Override
    public DataCell<Symbol>[][] doTransformMatrix(DataCell<Symbol>[][] verticalMatrix,
                                                  TransformMatrixConfig config,
                                                  BasePlaySession basePlaySession,
                                                  ISlotMachineConfig slotMachineConfig) {

        boolean isFoundWild = false;
        boolean isMatrixChanged = false;
        for (int reelIdx : config.reelChangeWild()) {
            isFoundWild = false;
            for (int rowIdx = 0, rowSize = verticalMatrix[reelIdx].length; rowIdx < rowSize; rowIdx++) {
                if (verticalMatrix[reelIdx][rowIdx] != null
                        && verticalMatrix[reelIdx][rowIdx].symbol() != null
                        && verticalMatrix[reelIdx][rowIdx].symbol().type() == SymbolType.WILD) {
                    isFoundWild = true;
                    break;
                }
            }
            if (isFoundWild) {
                this.expandWildAtReel(verticalMatrix, reelIdx,
                    config.slotMachineConfig().getSymbolByType(SymbolType.WILD));
                isMatrixChanged = true;
            }
        }
        
        if (isMatrixChanged) {
            return verticalMatrix;
        } else {
            return null;
        }
        
    }

    private void expandWildAtReel(DataCell<Symbol>[][] verticalMatrix, int atReel, Symbol wildSymbol) {
        for (int row = 0; row < verticalMatrix[atReel].length; row++) {
            if (verticalMatrix[atReel][row] != null) {
                verticalMatrix[atReel][row] = verticalMatrix[atReel][row].toBuilder().symbol(wildSymbol).build();
            }
        }
    }

}
