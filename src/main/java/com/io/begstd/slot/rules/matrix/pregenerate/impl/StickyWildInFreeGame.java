package com.io.begstd.slot.rules.matrix.pregenerate.impl;

import com.io.begstd.slot.model.app.BasePlaySession;
import com.io.begstd.slot.model.config.ISlotMachineConfig;
import com.io.begstd.slot.model.gamerule.DataCell;
import com.io.begstd.slot.model.gamerule.Symbol;
import com.io.begstd.slot.model.gamerule.SymbolType;
import com.io.begstd.slot.model.playsession.ISymbol;
import com.io.begstd.slot.rules.matrix.pregenerate.IPreGenerateMatrixRule;
import com.io.begstd.slot.utils.MatrixUtil;
import org.springframework.stereotype.Component;

import java.util.List;
@Component("stickyWildInFreeGame")
public class StickyWildInFreeGame implements IPreGenerateMatrixRule{

    /**
     * 
     */
    @Override
    public DataCell<Symbol>[][] preGenerateMatrix(BasePlaySession basePlaySession, ISlotMachineConfig config,
                                                  DataCell<Symbol>[][] verticalMatrix) {
        
        if (basePlaySession.freeGameMatrix() == null) {
            return verticalMatrix;
        }
        DataCell<ISymbol>[][] previousFreeGameAbstractMatrix = MatrixUtil.reverseMatrix(basePlaySession.freeGameMatrix());
        if (previousFreeGameAbstractMatrix == null) {
            return verticalMatrix;
        }
        DataCell<Symbol>[][] previousFreeGameMatrix = (DataCell<Symbol>[][]) MatrixUtil.convertToConcreteMatrixDataCell(Symbol.class, previousFreeGameAbstractMatrix);
        if (MatrixUtil.isMatrixEmpty(previousFreeGameMatrix))
            return verticalMatrix;

        if (!hasSymbolTypeInMatrix(previousFreeGameMatrix, SymbolType.WILD)) {
            return verticalMatrix;
        }
        // previous matrix has data and Wild
        List<Integer> freeGameTableFormat = basePlaySession.freeGameTableFormat();
        DataCell<Symbol>[][] resultVerticalMatrix = convertToStickyWild(previousFreeGameMatrix, verticalMatrix, freeGameTableFormat);
        return resultVerticalMatrix;
    }

    private DataCell<Symbol>[][] convertToStickyWild(DataCell<Symbol>[][] previousFreeGameMatrix, DataCell<Symbol>[][] preVerticalMatrix, List<Integer> tableFormat){
        DataCell<Symbol>[][] resultVerticalMatrix = MatrixUtil.cloneMatrix(preVerticalMatrix);
        for (int rowIndex = 0; rowIndex < tableFormat.size(); rowIndex++) {
            for (int colIndex = 0; colIndex < tableFormat.get(rowIndex); colIndex++) {
                if(previousFreeGameMatrix[rowIndex][colIndex] != null && 
                        previousFreeGameMatrix[rowIndex][colIndex].symbol() != null &&
                        previousFreeGameMatrix[rowIndex][colIndex].symbol().type().equals(SymbolType.WILD)) {
                        resultVerticalMatrix[rowIndex][colIndex] = DataCell.builder(Symbol.class)
                                            .symbol(previousFreeGameMatrix[rowIndex][colIndex].symbol())
                                            .type(previousFreeGameMatrix[rowIndex][colIndex].type())
                                            .build();
                 }
            }
        }
        return resultVerticalMatrix;
    }
    
    private boolean hasSymbolTypeInMatrix(DataCell<Symbol>[][] matrix, SymbolType type) {
        boolean hasSymbolType = false;
        for (DataCell<Symbol>[] rowSymbol : matrix) {
            for (DataCell<Symbol> dataCell : rowSymbol) {
               if(dataCell.symbol().type().equals(type)) {
                   hasSymbolType = true;
                   break;
               }
            }
            if(hasSymbolType) {
                break;
            }
        }
        return hasSymbolType;
    } 
}
