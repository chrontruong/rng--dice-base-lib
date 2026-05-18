package com.io.begstd.slot.model.config;

import com.io.begstd.slot.command.DenominationLevelCmd;
import com.io.begstd.slot.command.ExtraBetLevelCmd;
import com.io.begstd.slot.factory.GameRuleFactory;
import com.io.begstd.slot.machine.wonrule.WonRuleExtension;
import com.io.begstd.slot.model.domain.Money;
import com.io.begstd.slot.model.gamerule.*;
import com.io.begstd.slot.model.playsession.ISymbol;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * stored data from *_Free_Config.json
 *
 */
@Getter
@Accessors(fluent = true)
public final class SlotMachineConfigForFree implements ISlotMachineConfig, InitializingBean {

    private String serviceId;
    private int totalCredit;
    // define rule list: ex: lines, miniGame
    private List<String> wonRulesName;
    private boolean isWinFullLine;
    private int numSymbolWinBonusGame;// min of number to win a bonus game
    private int numSymbolWinFreeGame; // min of number to win a free game
    private int numPlayTotalInBonusGame;
    private List<Symbol> symbols;
    private List<PayLine> payLines;
    private int jackpotLineSize;
    private List<Integer> reelCheckForFreeGame;
    private List<Integer> reelCheckForBonusGame;
    private List<InitJackpotChild> initJackpotList;
    private List<DenominationLevelCmd> denominationLevels;
    // Symbol Code
    private String[][] baseReel; //default RTP 98

    private List<FreeSpinConfig> freeSpinConfig;

    // MatrixGenerateConfig
    private String defineMatrixFormatRule;
    private List<String> listPreGenerateMatrixRule;
    private List<ReelGenerateConfig> listReelConfig;
    private List<TransformMatrixConfig> listTransformMatrixConfig;

    // below variable outside config file
    private int reelSize;
    private int rowSize;

    private List<Integer> tableFormat;

    private List<Integer> reelCheckForLightningGame;
    private int numSymbolWinLightningGame; // min of number to win a Lightning game
    private int numPlayTotalInLightningGame;
    private List<String> lightningCodeSymbols;
    private List<String> rightCheckPayLineSymbolCode;
    private List<ISymbol> rightCheckPayLineSymbol;

    private List<Integer> reelCheckForPowerUpGame;

    @Autowired
    private GameRuleFactory gameRuleFactory;

    private List<WonRuleExtension> wonRules;

    private Symbol[][] baseSymbolReel;

    private String[][] baseReelTrial;
    private Symbol[][] baseSymbolReelTrial;
    private int totalPercent;
    /**
     * Transform to Symbol
     *
     * @return Symbol array[][]
     */
    public Symbol[][] getBaseReel() {

        return baseSymbolReel;
    }

    /**
     * Transform to Symbol for List in inputed matrix from FORM Test
     *
     * @param list
     * @return Symbol[]
     */
    public Symbol[] getSymbolsForTest(String[] list) {

        Map<String, Symbol> map = symbols.stream().collect(Collectors.toMap(Symbol::code, s -> s));

        Symbol[] temp = new Symbol[list.length];

        for (int j = 0; j < list.length; j++) {
            Symbol symbol = map.get(list[j]);
            temp[j] = symbol;
        }

        return temp;
    }

    /**
     * get free spin option from normal and free config
     */
    public FreeSpinConfig getFreeSpinConfig(int id) {
        for (FreeSpinConfig item : this.freeSpinConfig()) {
            if (item.id() == id) {
                return item;
            }
        }
        return null;
    }

    /**
     * convert rulelist to List
     * @return String List
     */
//    public List<String> getRuleListConfig() {
//        String[] temp = this.ruleList.split(" ");
//        return Arrays.asList(temp);
//    }
//
//    public List<GameWonRule> getAmountWonRule() {
//        return gameRules;
//    }

    @Override
    public void afterPropertiesSet() throws Exception {
        init();
    }

    public void init() {
        initWinGameRules();
        initBaseSymbolReelFromBaseReel();
        initBaseSymbolReelFromBaseReelTrial();
        initScreenSize();
        initTableFormat();
        initRightCheckPayLineSymbol();
    }

    private void initWinGameRules() {
        wonRules = this.gameRuleFactory.getGameWonRules(this.wonRulesName);
    }

    private void initBaseSymbolReelFromBaseReel() {
        Map<String, Symbol> map = symbols.stream().collect(Collectors.toMap(Symbol::code, s -> s));
        Symbol[][] baseSymbolReel = new Symbol[baseReel.length][];

        for (int i = 0; i < baseReel.length; i++) {
            baseSymbolReel[i] = new Symbol[baseReel[i].length];

            for (int j = 0; j < baseReel[i].length; j++) {
                Symbol symbol = map.get(baseReel[i][j]);
                baseSymbolReel[i][j] = symbol;
            }

        }
        this.baseSymbolReel = baseSymbolReel;
    }

    private void initScreenSize() {
        this.reelSize = this.listReelConfig.size();
        this.rowSize = this.listReelConfig.stream().map( (reelConfig) -> {
            return reelConfig.size();
        }).reduce( (size1, size2) -> {
            return size1 > size2 ? size1 : size2;
        }).get();
    }

    private void initTableFormat() {
        this.tableFormat = Arrays.asList(new Integer[this.listReelConfig.size()]);

        for(ReelGenerateConfig reelConfig : this.listReelConfig) {
            this.tableFormat.set(reelConfig.reelIdx(), reelConfig.size());
        }
    }

    private void initRightCheckPayLineSymbol() {
        if(!CollectionUtils.isEmpty(rightCheckPayLineSymbolCode)) {
            this.rightCheckPayLineSymbol = new ArrayList<ISymbol>();
            Map<String, Symbol> map = symbols.stream().collect(Collectors.toMap(Symbol::code, s -> s));
            for (String code: rightCheckPayLineSymbolCode) {
                this.rightCheckPayLineSymbol.add(map.get(code));

            }
        }
    }


    @Override
    public Symbol getSymbolByType(SymbolType type) {
        return this.symbols.stream().filter( (symbol) -> {
            return symbol.type() == type;
        }).findFirst().get();
    }

    @Override
    public List<DenominationLevel> getDenominationLevels() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<DenominationLevel> getDenominationLevelsForBet(String currency) {
        // TODO Auto-generated method stub
        return null;
    }

    public List<String> getLightningSymbolCodes( ) {
        return  this.lightningCodeSymbols;
    }

    @Override
    public boolean isValidBet(String betId, String currency) {
        return true;
    }

    public Symbol[][] getBaseReelTrial() {

        return baseSymbolReelTrial;
    }

    private void initBaseSymbolReelFromBaseReelTrial() {
        Map<String, Symbol> map = symbols.stream().collect(Collectors.toMap(Symbol::code, s -> s));
        if(baseReelTrial == null) {
            this.baseSymbolReelTrial = null;
            return;
        }
        Symbol[][] baseSymbolReel = new Symbol[baseReelTrial.length][];

        for (int i = 0; i < baseReelTrial.length; i++) {
            baseSymbolReel[i] = new Symbol[baseReelTrial[i].length];

            for (int j = 0; j < baseReelTrial[i].length; j++) {
                Symbol symbol = map.get(baseReelTrial[i][j]);
                baseSymbolReel[i][j] = symbol;
            }

        }
        this.baseSymbolReelTrial = baseSymbolReel;
    }

    @Override
    public SlotConfigType getConfigType() {
        return SlotConfigType.BASE_FREE;
    }
    @Override
    public Map<String, Money> getJACKPOTS() {
        // TODO Auto-generated method stub
        return null;
    }
    @Override
    public ExtraBetLevelCmd getExtraBetLevelById(String id) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override   
    public CurrencyType getAvailableCurrency(String inputCurrency) {    
        return null;    
    }   
    @Override   
    public List<CurrencyType> currencyType() {  
        return null;    
    }   
    @Override   
    public Map<String, Money> getJACKPOTSByCurrency(String currency) {  
        return null;    
    }
}
