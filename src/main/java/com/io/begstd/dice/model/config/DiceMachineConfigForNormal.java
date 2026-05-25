package com.io.begstd.dice.model.config;

import com.io.begstd.dice.factory.GameRuleFactory;
import com.io.begstd.dice.machine.wonrule.WonRuleExtension;
import com.io.begstd.dice.model.app.BigWinInfo;
import com.io.begstd.dice.model.domain.Money;
import com.io.begstd.dice.model.gamerule.DenominationLevel;
import com.io.begstd.dice.model.gamerule.InitJackpotChild;
import com.io.begstd.dice.model.gamerule.RTPDiceConfig;
import com.io.begstd.dice.command.DenominationLevelCmd;
import com.io.begstd.dice.command.ExtraBetLevelCmd;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * stored data from *_Normal_Config.json
 *
 */
@Getter
@Accessors(fluent = true)
public final class DiceMachineConfigForNormal implements IDiceMachineConfig, InitializingBean {

    private String serviceId;
    private String serviceCode;
    private String serviceName;
    private String prefixService;
    private String stateType;
    private List<String> wonRulesName;
    private int totalCredit;
    private int totalPercent;
    private int jackpotLineSize;
    private String rtpDefault;
    private List<RTPDiceConfig> rtpDiceConifgs;
    // Parse from json
    private List<DenominationLevelCmd> denominationLevels;
    private List<ExtraBetLevelCmd> extraBetLevels;
 // support currency
    private List<CurrencyType> currencyType;
    // Symbol Code
    private String[][] baseReel; //baseReel for default 98
    private int intervalGame;

    // init jackpot
    private List<InitJackpotChild> initJackpotList;

    private Map<String, Money> JACKPOTS = new HashMap<>();

    @Autowired
    private GameRuleFactory gameRuleFactory;

    private List<WonRuleExtension> wonRules;

    private double initTrialWalet;
    private List<InitJackpotChild> initTrialJackpotList;

    private List<BigWinInfo> bigWinInfos;

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
        if (currency.equals(DiceCurrencyType.ALL.name())) {
            return denominationLevels.stream()      
                    .map(cmd -> new DenominationLevel(cmd.indx(), cmd.id(), Money.of(cmd.amount()), cmd.jackpotID(), Money.ZERO, cmd.env(), cmd.curr()))
                    .collect(Collectors.toList());      
          } 
        return denominationLevels.stream()      
                .filter(denom -> currency.equals(denom.curr()))     
                .map(denom -> new DenominationLevel(denom.indx(), denom.id(), Money.of(denom.amount()), denom.jackpotID(), Money.ZERO, denom.env(), denom.curr()))
                .collect(Collectors.toList());
    }

    private List<DenominationLevelCmd> getDenominationByCurrency(String currency){      
        if (currency.equals(DiceCurrencyType.ALL.name())) {
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

    @Override
    public List<BigWinInfo> getBigWinInfos() {
        return bigWinInfos;
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
    public DiceConfigType getConfigType() {
        return DiceConfigType.BASE_NORMAL;
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
