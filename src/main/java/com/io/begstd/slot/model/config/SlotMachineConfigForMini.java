package com.io.begstd.slot.model.config;

import com.io.begstd.slot.model.gamerule.SymbolBonusGame;
import com.io.begstd.slot.model.playsession.ISymbolBonusGame;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.springframework.beans.factory.InitializingBean;

import java.util.ArrayList;
import java.util.List;
/**
 * stored data from SlotGame_Mini_Config.json
 *
 */
@Getter
@Accessors(fluent = true)
public final class SlotMachineConfigForMini implements IMiniSlotConfig, InitializingBean {

    private String serviceId;
    private int numOfBonusWonMini;
    private int rangeRandom; // min of number to win a minigame
    private int numPlayTotalInBonusGame;
    private int bonusMatrixSize;
    private List<Integer> bonusGameTableFormat;
    // mini game
    private List<SymbolBonusGame> symbolMini;
    private List<String> baseMini;

    private List<ISymbolBonusGame> baseBonusSymbolReel;

    public SymbolBonusGame getSymbolMiniById(int id) {
        if (symbolMini == null)
            return null;
        return symbolMini.stream().filter( (symbolMini) -> {
            return symbolMini.id() == id;
        }).findFirst().orElse(null);
    }

    public SymbolBonusGame getSymbolMiniByValue(float value) {
        if (symbolMini == null)
            return null;
        return symbolMini.stream().filter( (symbolMini) -> {
            return symbolMini.value() == value;
        }).findFirst().orElse(null);
    }

    public SymbolBonusGame getSymbolMiniByCode(String code) {
        if (symbolMini == null)
            return null;
        return symbolMini.stream().filter( (symbolMini) -> {
            return symbolMini.code().equalsIgnoreCase(code);
        }).findFirst().orElse(null);
    }

    public void init() {

        List<ISymbolBonusGame> baseReelOfBonusGame = new ArrayList<>();
        for (int i = 0; i < baseMini.size(); i++) {
            SymbolBonusGame miniBonus = getSymbolMiniByCode(baseMini.get(i));
            baseReelOfBonusGame.add(miniBonus);
        }
        this.baseBonusSymbolReel = baseReelOfBonusGame;
    }


    @Override
    public void afterPropertiesSet() throws Exception {
        init();

    }

    @Override
    public SlotConfigType getConfigType() {
        return SlotConfigType.BASE_MINI;
    }
}
