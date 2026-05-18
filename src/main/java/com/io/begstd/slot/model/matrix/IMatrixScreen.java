package com.io.begstd.slot.model.matrix;

import com.io.begstd.slot.model.gamerule.DataCell;
import com.io.begstd.slot.model.gamerule.PayLine;
import com.io.begstd.slot.model.gamerule.Symbol;
import com.io.begstd.slot.model.gamerule.SymbolType;
import com.io.begstd.slot.model.playsession.ISymbol;
import org.javatuples.Pair;

import java.util.List;

public interface IMatrixScreen {
    public int reelSize();
    public int rowSize();

    public Symbol[][] horizontalMatrix();
    
    public List<Symbol> findRowByPerLine(PayLine payLine);
    public Pair<Symbol, Integer> countSymbolInLine(List<Symbol> wonRow);
    public Pair<Symbol, Integer> countSymbolInLine(List<Symbol> wonRow, SymbolType type);
    public Pair<Symbol, Integer> countSymbolByType(SymbolType type);
    public Pair<Symbol, Integer> countSymbolInLineBySymbolType(List<Symbol> rowMatchBetLine, SymbolType type);
    
    public DataCell<Symbol>[][] horizontalMatrixDataCell();
    
    public List<Symbol> findRowByPerLineRight(PayLine payLine, List<ISymbol> needToRightPayLine);
    public Pair<Symbol, Integer> countSymbolInLineRight(List<Symbol> wonRow);
}
