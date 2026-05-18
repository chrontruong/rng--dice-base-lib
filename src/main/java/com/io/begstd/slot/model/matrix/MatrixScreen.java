package com.io.begstd.slot.model.matrix;

import com.io.begstd.slot.model.gamerule.DataCell;
import com.io.begstd.slot.model.gamerule.PayLine;
import com.io.begstd.slot.model.gamerule.Symbol;
import com.io.begstd.slot.model.gamerule.SymbolType;
import com.io.begstd.slot.model.playsession.ISymbol;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.javatuples.Pair;
import org.springframework.util.CollectionUtils;

import java.util.Arrays;
import java.util.List;

@Data
@Accessors(fluent = true)
@Slf4j
public class MatrixScreen implements IMatrixScreen {

    private DataCell<Symbol>[][] horizontalMatrixDataCell;
    private Symbol[][] horizontalMatrix;
    
    public MatrixScreen(DataCell<Symbol>[][] horizontalMatrixDataCell) {
        this.horizontalMatrixDataCell = horizontalMatrixDataCell;
        
        this.horizontalMatrix = new Symbol[horizontalMatrixDataCell.length][];
        for (int rowIdx = 0, rowSize = horizontalMatrixDataCell.length; rowIdx < rowSize; rowIdx++) {
            this.horizontalMatrix[rowIdx] = new Symbol[horizontalMatrixDataCell[rowIdx].length];
            for (int reelIdx = 0, reelSize = horizontalMatrixDataCell[rowIdx].length; reelIdx < reelSize; reelIdx++ ) {
                if ( horizontalMatrixDataCell[rowIdx][reelIdx] != null) {
                    this.horizontalMatrix[rowIdx][reelIdx] = (Symbol) horizontalMatrixDataCell[rowIdx][reelIdx].symbol();
                }
                
            }
        }
    }

    @Override
    public int reelSize() {
        return horizontalMatrix[0].length;
    }

    @Override
    public int rowSize() {
        return horizontalMatrix.length;
    }

    @Override
    public List<Symbol> findRowByPerLine(PayLine payLine) {
        int[][] verifyView = payLine.verifyView();

        Symbol[][] view = this.horizontalMatrix;

        Symbol[] wonRow = new Symbol[verifyView[0].length];

        for (int rowIdx = 0; rowIdx < verifyView.length; rowIdx++) {
            for (int reelIdx = 0; reelIdx < verifyView[rowIdx].length; reelIdx++) {
                if (verifyView[rowIdx][reelIdx] == 1 && rowIdx < view.length) {
                    try {
                        wonRow[reelIdx] = view[rowIdx][reelIdx];
                    } catch (Exception e) {
                        log.error(e.getMessage() , e);
                    }
                }
            }
        }

        return Arrays.asList(wonRow);
    }

    @Override
    public List<Symbol> findRowByPerLineRight(PayLine payLine, List<ISymbol> needToRightPayLines) {
        int[][] verifyView = payLine.verifyView();

        Symbol[][] view = this.horizontalMatrix;

        Symbol[] wonRow = new Symbol[verifyView[0].length];

        for (int rowIdx = 0; rowIdx < verifyView.length; rowIdx++) {
            for (int reelIdx = verifyView[rowIdx].length -1; reelIdx >=0; reelIdx--) {
                if (verifyView[rowIdx][reelIdx] == 1 &&
                        !CollectionUtils.isEmpty(needToRightPayLines)) {
                    for(ISymbol needToRightPayLine : needToRightPayLines) {
                        if(needToRightPayLine != null &&
                           rowIdx < view.length && 
                        (view[rowIdx][reelIdx].code().equals(needToRightPayLine.code() ) ||
                         SymbolType.WILD.equals(view[rowIdx][reelIdx].type()))) {
                            try {
                                wonRow[reelIdx] = view[rowIdx][reelIdx];
                                break;
                            } catch (Exception e) {
                                log.error(e.getMessage() , e);
                            }
                         }
                    }
                }
            }
        }

        return Arrays.asList(wonRow);
    }
    
    /**
     * count symbol in the line
     * 
     * @param wonRow
     * @return
     */
    @Override
    public Pair<Symbol, Integer> countSymbolInLine(List<Symbol> wonRow) {

        /*
         * How it works :D count = 0 0 == 1 count = 1 1 == 2 count = 2 2 == 3 count = 3
         * 3 == 4 count = 4
         */
        int count = 0;
        // new alogirthm
        Symbol symbolFirst = null;
        for (int i = 0; i < wonRow.size(); i++) {
            if (wonRow.get(i) != null && (wonRow.get(i).type() != SymbolType.WILD)) {
                symbolFirst = wonRow.get(i);
                break;
            } else if(wonRow.get(i) == null) {
                break;
            }
        }
        // khong tinh win line cho con scatter
        if (symbolFirst == null) {
            // khong tim thay con khac WILD == wild co full line - wild khong co paytable
            return new Pair<>(null, 0);
        } else {
            if (symbolFirst.type() == SymbolType.SCATTER || symbolFirst.type() == SymbolType.BONUS) {
                return new Pair<>(symbolFirst, count);
            }
            // count cho truong hop symbolFirst != scatter
            for (int i = 0; i < wonRow.size(); i++) {
                Symbol symbol = wonRow.get(i);
                if (symbol != null && ((symbol.type() == (SymbolType.WILD)) || (symbol.equalTo(symbolFirst)))) {
                    count++;
                } else {
                    break;
                }
            }
        }
        return new Pair<>(symbolFirst, count - 1);
    }

    @Override
    public Pair<Symbol, Integer> countSymbolInLineRight(List<Symbol> wonRow) {

        /*
         * How it works :D count = 0 0 == 1 count = 1 1 == 2 count = 2 2 == 3 count = 3
         * 3 == 4 count = 4
         */
        int count = 0;
        // new alogirthm
        Symbol symbolFirst = null;
        for (int i = wonRow.size() -1; i >= 0; i--) {
            if(wonRow.get(i) != null && (wonRow.get(i).type() != SymbolType.WILD)) {
                symbolFirst = wonRow.get(i);
                break;
            } else if(wonRow.get(i) == null) {
                break;
            }
        }
        // khong tinh win line cho con scatter
        if (symbolFirst == null) {
            // khong tim thay con khac WILD == wild co full line - wild khong co paytable
            return new Pair<>(null, 0);
        } else {
            if (symbolFirst.type() == SymbolType.SCATTER || symbolFirst.type() == SymbolType.BONUS) {
                return new Pair<>(symbolFirst, count);
            }
            // count cho truong hop symbolFirst != scatter
            for (int i = wonRow.size()-1; i >= 0 ; i--) {
                Symbol symbol = wonRow.get(i);
                if (symbol !=  null && ((symbol.type() == (SymbolType.WILD)) || (symbol.equalTo(symbolFirst)))) {
                    count++;
                } else {
                    break;
                }
            }
        }
        return new Pair<>(symbolFirst, count - 1);
    }
    
    @Override
    public Pair<Symbol, Integer> countSymbolByType(SymbolType type) {
        Symbol countedSymbol = null;
        int count = 0;
        for (Symbol[] symbols : this.horizontalMatrix) {
            for (Symbol symbol : symbols) {
                if (symbol != null && symbol.type() == type) {
                    countedSymbol = symbol;
                    count++;
                }
            }
        }
        return new Pair<>(countedSymbol, count);
    }

    @Override
    public Pair<Symbol, Integer> countSymbolInLine(List<Symbol> wonRow, SymbolType type) {
        return null;
    }

    @Override
    public Pair<Symbol, Integer> countSymbolInLineBySymbolType(List<Symbol> wonRow, SymbolType type) {
        int count = 0;
        // count cho truong hop symbolFirst = wild
        for (int i = 0; i < wonRow.size(); i++) {
            Symbol symbol = wonRow.get(i);
            if (symbol.type() == type) {
                count++;
            } else {
                break;
            }
        }

        return new Pair<>(wonRow.get(0), count - 1);
    }
}
