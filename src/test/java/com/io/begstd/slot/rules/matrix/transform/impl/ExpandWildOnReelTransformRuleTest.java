package com.io.begstd.slot.rules.matrix.transform.impl;

import com.io.begstd.slot.AbstractBaseSlotMockTest;
import com.io.begstd.slot.model.config.ISlotMachineConfig;
import com.io.begstd.slot.model.config.TransformMatrixConfig;
import com.io.begstd.slot.model.gamerule.DataCell;
import com.io.begstd.slot.model.gamerule.Symbol;
import com.io.begstd.slot.model.gamerule.SymbolType;
import com.io.begstd.slot.model.matrix.MatrixScreen;
import com.io.begstd.slot.rules.matrix.transform.ITransformMatrixRule;
import com.io.begstd.slot.utils.MatrixUtil;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

public class ExpandWildOnReelTransformRuleTest extends AbstractBaseSlotMockTest {

    @Autowired
    @Qualifier("slotMachineConfigForNormal")
    private ISlotMachineConfig slotMachineConfig;
    
    @Autowired
    private ITransformMatrixRule transformMatrixRule;
    
    private DataCell<Symbol> K; // Wild symbol
    private DataCell<Symbol> R; // bonus symbol
    private DataCell<Symbol> A; // Scatter symbol
    private DataCell<Symbol> O; // normal symbol
    private DataCell<Symbol> N; // null symbol
    
    private DataCell.DataCellBuilder<Symbol> builder;
    
    @Before
    public void before() {
        builder = DataCell.builder(Symbol.class);
        N = builder.build();
        K = builder.symbol(slotMachineConfig.getSymbolByType(SymbolType.WILD)).build();
        R = builder.symbol(slotMachineConfig.getSymbolByType(SymbolType.BONUS)).build();
        A = builder.symbol(slotMachineConfig.getSymbolByType(SymbolType.SCATTER)).build();
        O = builder.symbol(slotMachineConfig.getSymbolByType(SymbolType.SYMBOL)).build();
    }
    
    @Test
    public void expandWildOnReelHasWildAndNoTNullTest() {
        TransformMatrixConfig config = new TransformMatrixConfig();
        
        int[] reelChangeWild = {2};
        
        config.reelChangeWild(reelChangeWild);
        config.slotMachineConfig(slotMachineConfig);
        
        @SuppressWarnings("unchecked")
        DataCell<Symbol>[][] originVerticalMatrixDataCell = (DataCell<Symbol>[][]) new DataCell[][]{
                                                {O  ,R  ,O  ,O  ,O  },
                                                {O  ,A  ,O  ,O  ,O  },
                                                {K  ,O  ,O  ,O  ,O  },
                                                {O  ,O  ,A  ,O  ,O  },
                                                {O  ,O  ,O  ,R  ,O  }
                                                                      };
        DataCell<Symbol>[][] verticalMatrixDataCell = transformMatrixRule.transformMatrix(originVerticalMatrixDataCell, config, null, configForNormal);
        
        MatrixScreen matrixScreen = new MatrixScreen(MatrixUtil.reverseMatrix(verticalMatrixDataCell));
        Symbol[][] horizontalMatrix = matrixScreen.horizontalMatrix();
        
        // different reference
        Assert.assertTrue(originVerticalMatrixDataCell != verticalMatrixDataCell);
        
        // all symbol not null
        for (Symbol[] rowSymbol : horizontalMatrix) {
            for (Symbol symbol : rowSymbol) {
                Assert.assertNotNull(symbol);
            }
        }
        
        for (int reelIdx : reelChangeWild) {
            for (int rowIdx = 0; rowIdx < horizontalMatrix.length; rowIdx++) {
                Assert.assertEquals(horizontalMatrix[rowIdx][reelIdx], K.symbol());
            }
        }
    }
    
    @Test
    public void expandWildOnReelHasWildAndHasNullTest() {
        TransformMatrixConfig config = new TransformMatrixConfig();
        
        int[] reelChangeWild = {2};
        
        config.reelChangeWild(reelChangeWild);
        config.slotMachineConfig(slotMachineConfig);
        
        @SuppressWarnings("unchecked")
        DataCell<Symbol>[][] originVerticalMatrixDataCell = (DataCell<Symbol>[][]) new DataCell[][]{
                                                        {O  ,R  ,O  ,O  ,O  },
                                                        {O  ,A  ,O  ,O  ,O  },
                                                        {K  ,O  ,O  ,N  ,N  },
                                                        {O  ,O  ,A  ,O  ,R  },
                                                        {O  ,O  ,O  ,R  ,O  }
                                                                              };

        DataCell<Symbol>[][] verticalMatrixDataCell = transformMatrixRule.transformMatrix(originVerticalMatrixDataCell, config, null, configForNormal);
        
        MatrixScreen matrixScreen = new MatrixScreen(MatrixUtil.reverseMatrix(verticalMatrixDataCell));
        Symbol[][] horizontalMatrix = matrixScreen.horizontalMatrix();
        
        // different reference
        Assert.assertTrue(originVerticalMatrixDataCell != verticalMatrixDataCell);
        
        for (int reelIdx : reelChangeWild) {
            for (int rowIdx = 0; rowIdx < horizontalMatrix.length; rowIdx++) {
                if (horizontalMatrix[rowIdx][reelIdx] != null) {
                    Assert.assertEquals(horizontalMatrix[rowIdx][reelIdx], K.symbol());
                } else {
                    Assert.assertEquals(horizontalMatrix[rowIdx][reelIdx], originVerticalMatrixDataCell[rowIdx][reelIdx].symbol());
                }
            }
        }
    }
    
    @Test
    public void expandWildOnReelHasNoWildTest() {
        TransformMatrixConfig config = new TransformMatrixConfig();
        
        int[] reelChangeWild = {0, 1, 2, 3, 4};
        
        config.reelChangeWild(reelChangeWild);
        config.slotMachineConfig(slotMachineConfig);
        
        @SuppressWarnings("unchecked")
        DataCell<Symbol>[][] originVerticalMatrixDataCell = (DataCell<Symbol>[][]) new DataCell[][]{
                                                        {O  ,R  ,O  ,O  ,O  },
                                                        {O  ,A  ,O  ,O  ,O  },
                                                        {O  ,O  ,O  ,O  ,O  },
                                                        {O  ,O  ,A  ,O  ,O  },
                                                        {O  ,O  ,O  ,R  ,O  }
                                                                              };
        
        DataCell<Symbol>[][] verticalMatrixDataCell = transformMatrixRule.transformMatrix(originVerticalMatrixDataCell, config, null, configForNormal);
        
        // different reference
        Assert.assertTrue(originVerticalMatrixDataCell != verticalMatrixDataCell);
        
        // Not transform, expect matrix is null
        Assert.assertNull(verticalMatrixDataCell);
    }
    
    @Test
    public void expandWildOnReelHasNoWildAndHasNullTest() {
        TransformMatrixConfig config = new TransformMatrixConfig();
        
        int[] reelChangeWild = {0, 1, 2, 3, 4};
        
        config.reelChangeWild(reelChangeWild);
        config.slotMachineConfig(slotMachineConfig);
        
        @SuppressWarnings("unchecked")
        DataCell<Symbol>[][] originVerticalMatrixDataCell = (DataCell<Symbol>[][]) new DataCell[][]{
                                                        {O  ,O  ,O  ,N  ,N  },
                                                        {R  ,A  ,O  ,O  ,N  },
                                                        {O  ,O  ,O  ,A  ,O  },
                                                        {O  ,O  ,O  ,O  ,N  },
                                                        {O  ,O  ,O  ,N  ,N  }
                                                                              };
        
        DataCell<Symbol>[][] verticalMatrixDataCell = transformMatrixRule.transformMatrix(originVerticalMatrixDataCell, config, null, configForNormal);
        
        // different reference
        Assert.assertTrue(originVerticalMatrixDataCell != verticalMatrixDataCell);
        
        // Not transform, expect matrix is null
        Assert.assertNull(verticalMatrixDataCell);
    }
}
