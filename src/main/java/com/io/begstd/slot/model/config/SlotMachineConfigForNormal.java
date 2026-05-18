package com.io.begstd.slot.model.config;

import com.io.begstd.slot.command.DenominationLevelCmd;
import com.io.begstd.slot.command.ExtraBetLevelCmd;
import com.io.begstd.slot.factory.GameRuleFactory;
import com.io.begstd.slot.machine.wonrule.WonRuleExtension;
import com.io.begstd.slot.model.app.BigWinInfo;
import com.io.begstd.slot.model.domain.Money;
import com.io.begstd.slot.model.gamerule.*;
import com.io.begstd.slot.model.playsession.ISymbol;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * stored data from *_Normal_Config.json
 *
 */
@Getter
@Accessors(fluent = true)
public final class SlotMachineConfigForNormal implements ISlotMachineConfig, InitializingBean {

    private String serviceId;
    private String serviceCode;
    private String serviceName;
    private String prefixService;
    private String stateType;
    private List<String> wonRulesName;
    private boolean isWinFullLine;
    private int totalCredit;
    private List<Symbol> symbols;
    private List<PayLine> payLines;
    private int jackpotLineSize;
    private String rtpDefault;
    private List<RTPSlotConfig> rtpSlotConifgs;
    // Parse from json
    private List<DenominationLevelCmd> denominationLevels;
    private List<ExtraBetLevelCmd> extraBetLevels;
 // support currency
    private List<CurrencyType> currencyType;
    // Symbol Code
    private String[][] baseReel; //baseReel for default 98
    private int intervalGame;

    // the new step before play freegame
    private List<FreeSpinOption> freeSpinOption;

    // init jackpot
    private int totalPercent;
    private List<InitJackpotChild> initJackpotList;

    private int numSymbolWinBonusGame; // min of number to win a minigame
    private int numSymbolWinFreeGame; // min of number to win a free game
    private int numPlayTotalInBonusGame;

    private List<Integer> reelCheckForFreeGame;
    private List<Integer> reelCheckForBonusGame;
    private List<FreeSpinConfig> freeSpinConfig;

    // MatrixGenerateConfig
    private String defineMatrixFormatRule;
    private List<String> listPreGenerateMatrixRule;
    private List<ReelGenerateConfig> listReelConfig;
    private List<TransformMatrixConfig> listTransformMatrixConfig;

    //below variable is not in config file
    private int reelSize;
    private int rowSize;

    private List<Integer> tableFormat;

    private Map<String, Money> JACKPOTS = new HashMap<>();

//    private List<String> lightningWonRules;
    private List<Integer> reelCheckForLightningGame;
    private int numSymbolWinLightningGame; // min of number to win a Lightning games
    private int numPlayTotalInLightningGame;
    private List<String> lightningCodeSymbols;
    private List<String> rightCheckPayLineSymbolCode;
    private List<ISymbol> rightCheckPayLineSymbol;

    private List<Integer> reelCheckForPowerUpGame;

    @Autowired
    private GameRuleFactory gameRuleFactory;

    private List<WonRuleExtension> wonRules;

    private Symbol[][] baseSymbolReel;

    /**
     * Transform to Symbol
     *
     * @return Symbol array[][]
     */
    public Symbol[][] getBaseReel() {
        return baseSymbolReel;
    }

    private double initTrialWalet;
    private List<InitJackpotChild> initTrialJackpotList;

    private String[][] baseReelTrial;

    private Symbol[][] baseSymbolReelTrial;

    private List<BigWinInfo> bigWinInfos;

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
            Symbol symbol = map.get(list[j].trim());
            temp[j] = symbol;
        }

        return temp;
    }

    /**
     * get free spin config
     */
    public FreeSpinConfig getFreeSpinConfig(int id) {
        for (FreeSpinConfig item : this.freeSpinConfig()) {
            if (item.id() == id) {
                return item;
            }
        }
        return null;
    }

    public List<String> getDenominationLevelsForEnv(int env){
        List<String> result = new ArrayList<String>();
        for (DenominationLevelCmd cmd : denominationLevels) {
            if (cmd.env().contains(env)) {
                result.add(cmd.id() + ";" + cmd.amount().multiply(BigDecimal.valueOf(this.totalCredit)));
            }
        }
        return result;
    }

    public boolean isValidBet(String betId){
        if (extraBetLevels == null || extraBetLevels.size() == 0) {
            for (DenominationLevelCmd cmd : denominationLevels) {
                if (cmd.id().equals(betId)) {
                    return true;
                }
            }
        } else {
            for (DenominationLevelCmd cmd : denominationLevels) {
                for (ExtraBetLevelCmd exCmd : extraBetLevels) {
                    int betIdInt = Integer.parseInt(cmd.id()) + Integer.parseInt(exCmd.id());
                    if (betId.equals(String.valueOf(betIdInt))) {
                        return true;
                    }
                }

            }
        }
        return false;
    }

    public List<String> getExtraBetForEnv(){
        List<String> result = new ArrayList<String>();
        if (extraBetLevels != null) {
            for (ExtraBetLevelCmd extra : extraBetLevels) {
                result.add(extra.id() + ";" + extra.amount());
            }
        }
        return result;
    }
    /**
     * Transform from config to Model
     *
     * @return BetPerLine List
     */
    public List<DenominationLevel> getDenominationLevels() {
        // create jackpot list from denominationLevels and initJackpotList: one denom
        // level has 4 init levels
        List<DenominationLevel> result = new ArrayList<DenominationLevel>();
        for (DenominationLevelCmd cmd : denominationLevels) {
            for (InitJackpotChild jpItem : initJackpotList) {
                result.add(new DenominationLevel(cmd.indx(), cmd.id(), Money.of(cmd.amount()), cmd.jackpotID() + jpItem.code(),
                    Money.of(cmd.amount()).multiply(jpItem.initAmount()), cmd.env(), cmd.curr()));
            }
        }
        return result;
    }

    public List<DenominationLevel> getDenominationLevelsForBet(String currency) {

//        return denominationLevels.stream()
//                .map(cmd -> new DenominationLevel(cmd.id(), Money.of(cmd.amount()), cmd.jackpotID(), Money.ZERO, cmd.env()))
//                .collect(Collectors.toList());
        if (currency.equals(SlotCurrencyType.ALL.name())) {     
            return denominationLevels.stream()      
                    .map(cmd -> new DenominationLevel(cmd.indx(), cmd.id(), Money.of(cmd.amount()), cmd.jackpotID(), Money.ZERO, cmd.env(), cmd.curr()))    
                    .collect(Collectors.toList());      
          } 
        return denominationLevels.stream()      
                .filter(denom -> currency.equals(denom.curr()))     
                .map(denom -> new DenominationLevel(denom.indx(), denom.id(), Money.of(denom.amount()), denom.jackpotID(), Money.ZERO, denom.env(), denom.curr()))      
                .collect(Collectors.toList());
    }

    public List<Integer> getFreeSpinOptin() {
        List<Integer> result = new ArrayList<Integer>();
        for (FreeSpinOption opt : freeSpinOption) {
            result.add(opt.id());
        }
        return result;
    }
    private List<DenominationLevelCmd> getDenominationByCurrency(String currency){      
        if (currency.equals(SlotCurrencyType.ALL.name())) {     
            return denominationLevels;      
        }       
                
        return denominationLevels.stream()      
          .filter(denom -> currency.equals(denom.curr()))       
          .collect(Collectors.toList());        
                
    }
    public List<String> getDenominationLevelsForEnv(int env, String currency) {     
        
        List<String> result = new ArrayList<String>();      
        // get denom list by currency       
        List<DenominationLevelCmd> denomByCurrency = this.getDenominationByCurrency(currency);      
                
        for (DenominationLevelCmd cmd : denomByCurrency) {      
            if (cmd.env().contains(env)) {      
                result.add(cmd.id() + ";" + cmd.amount().multiply(BigDecimal.valueOf(this.totalCredit)));       
            }
        }
        return result;
    }
    
    public boolean isValidBet(String betId, String currency) {      
        // get denom list by currency       
        List<DenominationLevelCmd> denomByCurrency = this.getDenominationByCurrency(currency);      
        if (extraBetLevels == null || extraBetLevels.size() == 0) {     
            for (DenominationLevelCmd cmd : denomByCurrency) {      
                if (cmd.id().equals(betId)) {       
                    return true;        
                }       
            }       
        } else {        
            for (DenominationLevelCmd cmd : denomByCurrency) {      
                for (ExtraBetLevelCmd exCmd : extraBetLevels) {     
                    int betIdInt = Integer.parseInt(cmd.id()) + Integer.parseInt(exCmd.id());       
                    if (betId.equals(String.valueOf(betIdInt))) {       
                        return true;        
                    }       
                }       
            }       
        }       
        return false;       
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        init();
    }

    public void init() {
        initJackpot();
        initWinGameRules();
        initBaseSymbolReelFromBaseReel(); //default 98 rtp
        initBaseSymbolReelFromBaseReelTrial();
        initScreenSize();
        initTableFormat();
        initRightCheckPayLineSymbol();
    }

    private void initJackpot() {
        this.getDenominationLevels().forEach(betPerLine1 -> {
            JACKPOTS.put(betPerLine1.jackpotID(), betPerLine1.initJackpot());
        });
    }

    public Map<String, Money> getJACKPOTS() {
        return this.JACKPOTS;
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

    @Override
    public Symbol getSymbolByType(SymbolType type) {
        return this.symbols.stream().filter((symbol) -> {
            return symbol.type() == type;
        }).findFirst().get();
    }

    public ExtraBetLevelCmd getExtraBetLevelById(String id) {
        if(extraBetLevels == null) {
            return null;
        }
        return this.extraBetLevels.stream().filter(extraBetLevel ->
                extraBetLevel.id().equals(id)
        ).findFirst().orElse(null);
    }

    public InitJackpotChild getJackpotInitByCode(String code) {
        return initJackpotList.stream().filter((jacpotInit) -> {
            return jacpotInit.code().equals(code);
        }).findFirst().get();
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

    public List<String> getLightningSymbolCodes( ) {
        return  this.lightningCodeSymbols;
    }

    @Override
    public List<BigWinInfo> getBigWinInfos() {
        return bigWinInfos;
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
        Symbol[][] baseSymbolReelTrial = new Symbol[baseReelTrial.length][];

        for (int i = 0; i < baseReelTrial.length; i++) {
            baseSymbolReelTrial[i] = new Symbol[baseReelTrial[i].length];

            for (int j = 0; j < baseReelTrial[i].length; j++) {
                Symbol symbol = map.get(baseReelTrial[i][j]);
                baseSymbolReelTrial[i][j] = symbol;
            }

        }
        this.baseSymbolReelTrial = baseSymbolReelTrial;
    }

    public Map<String, Money> getJackpotListForTrial() {
        Map<String, Money> jackpotTrialList = new HashMap<>();
        for (Map.Entry<String, Money> entry : JACKPOTS.entrySet()) {
            InitJackpotChild itemTrial = this.getJackpotInitTrialByCode(entry.getKey());
            if (itemTrial != null) {
                jackpotTrialList.put(entry.getKey(), Money.of(itemTrial.initAmount()));
            } else {
                jackpotTrialList.put(entry.getKey(), entry.getValue());
            }
        }
        return jackpotTrialList;
    }

    private InitJackpotChild getJackpotInitTrialByCode(String code) {
        return initTrialJackpotList.stream().filter(jacpotInit ->
                jacpotInit.code().equals(code)
        ).findFirst().orElse(null);
    }

    @Override
    public SlotConfigType getConfigType() {
        return SlotConfigType.BASE_NORMAL;
    }

    public Map<String, Money> getJACKPOTSByCurrency(String currency) {
        Map<String, Money> jpCurrency = new HashMap<>();
        
        this.getDenominationLevels().forEach(betPerLine1 -> {
            if (betPerLine1.curr().equalsIgnoreCase(currency)) {
                jpCurrency.put(betPerLine1.jackpotID(), betPerLine1.initJackpot());
            }
        });
        
        return jpCurrency;
    }
    
    @Override
    public CurrencyType getAvailableCurrency(String inputCurrency) {
        if (StringUtils.isEmpty(inputCurrency)) {
            return getDefaultCurrency();
        }
        
        for(CurrencyType item : currencyType) {
            if (inputCurrency.equalsIgnoreCase(item.name())) {
                return item;
            }
        }
        // input wrong currency
        return null;
    }

    private CurrencyType getDefaultCurrency() {
        for(CurrencyType item : currencyType) {
            if ("DEFAULT".equalsIgnoreCase(item.type())) {
                return item;
            }
        }
        return currencyType.get(0);
    }

}
