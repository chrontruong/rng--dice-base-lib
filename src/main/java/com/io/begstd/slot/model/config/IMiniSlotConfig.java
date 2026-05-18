package com.io.begstd.slot.model.config;

import com.io.begstd.slot.model.gamerule.SymbolBonusGame;
import com.io.begstd.slot.model.playsession.ISymbolBonusGame;

import java.util.List;

public interface IMiniSlotConfig extends ICommonSlotConfig {
    String serviceId();
    int numOfBonusWonMini();
    int rangeRandom();
    int numPlayTotalInBonusGame();
    int bonusMatrixSize();
    List<Integer> bonusGameTableFormat();
    List<SymbolBonusGame> symbolMini();
    List<String> baseMini();
    List<ISymbolBonusGame> baseBonusSymbolReel();
    SymbolBonusGame getSymbolMiniById(int id);
    SymbolBonusGame getSymbolMiniByValue(float value);
    SymbolBonusGame getSymbolMiniByCode(String code);
}
