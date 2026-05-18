package com.io.begstd.slot.utils;

import com.io.begstd.slot.model.gamerule.DataCell;
import com.io.begstd.slot.model.gamerule.Symbol;
import com.io.begstd.slot.model.playsession.ISymbol;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
public class MatrixUtilsTest {
    @Test
    public void convertToConcreteMatrixDataCellTest() {
        Symbol.SymbolBuilder symbolBuilder = Symbol.builder();
        
        Symbol s1 = symbolBuilder.code("A").build();
        Symbol s2 = symbolBuilder.code("B").build();
        Symbol s3 = symbolBuilder.code("C").build();
        Symbol s4 = symbolBuilder.code("D").build();
        
        DataCell<Symbol>[][] dataCellSymbolMatrix = MatrixUtil.initDataCellMatrix(Symbol.class, 3, 4);
        
        dataCellSymbolMatrix[0][0] = DataCell.builder(Symbol.class).symbol(s1).build();
        dataCellSymbolMatrix[0][1] = DataCell.builder(Symbol.class).symbol(s2).build();
        dataCellSymbolMatrix[0][2] = DataCell.builder(Symbol.class).symbol(s3).build();
        dataCellSymbolMatrix[0][3] = DataCell.builder(Symbol.class).symbol(s4).build();
        
        dataCellSymbolMatrix[1][0] = DataCell.builder(Symbol.class).symbol(s1).build();
        dataCellSymbolMatrix[1][1] = DataCell.builder(Symbol.class).symbol(s2).build();
        dataCellSymbolMatrix[1][2] = DataCell.builder(Symbol.class).symbol(s3).build();
        dataCellSymbolMatrix[1][3] = DataCell.builder(Symbol.class).symbol(s4).build();
        
        dataCellSymbolMatrix[2][0] = DataCell.builder(Symbol.class).symbol(s1).build();
        dataCellSymbolMatrix[2][1] = DataCell.builder(Symbol.class).symbol(s2).build();
        dataCellSymbolMatrix[2][2] = DataCell.builder(Symbol.class).symbol(s3).build();
        dataCellSymbolMatrix[2][3] = DataCell.builder(Symbol.class).symbol(s4).build();
        
        DataCell<ISymbol>[][] dataCellAbstractSymbolMatrix = MatrixUtil.convertToAbstractMatrixDataCell(ISymbol.class, dataCellSymbolMatrix);
        
        for (int rowIdx = 0, rowSize = dataCellSymbolMatrix.length; rowIdx < rowSize; rowIdx++) {
            for (int colIdx = 0, colSize = dataCellSymbolMatrix[rowIdx].length; colIdx < colSize; colIdx++) {
                Assert.assertEquals(dataCellSymbolMatrix[rowIdx][colIdx], dataCellAbstractSymbolMatrix[rowIdx][colIdx]);
            }
        }
        
        @SuppressWarnings("unchecked")
        DataCell<Symbol>[][] newDataCellSymbolMatrix = (DataCell<Symbol>[][]) MatrixUtil.convertToConcreteMatrixDataCell(Symbol.class, dataCellAbstractSymbolMatrix);
        
        for (int rowIdx = 0, rowSize = dataCellSymbolMatrix.length; rowIdx < rowSize; rowIdx++) {
            for (int colIdx = 0, colSize = dataCellSymbolMatrix[rowIdx].length; colIdx < colSize; colIdx++) {
                Assert.assertEquals(dataCellAbstractSymbolMatrix[rowIdx][colIdx], newDataCellSymbolMatrix[rowIdx][colIdx]);
                Assert.assertEquals(dataCellSymbolMatrix[rowIdx][colIdx], newDataCellSymbolMatrix[rowIdx][colIdx]);
            }
        }
    }
}
