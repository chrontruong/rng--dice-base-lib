package com.io.begstd.dice.model.config;

import com.io.begstd.dice.machine.wonrule.WonRuleExtension;
import com.io.begstd.dice.model.app.BigWinInfo;
import com.io.begstd.dice.model.domain.Money;
import com.io.begstd.dice.model.gamerule.DenominationLevel;
import com.io.begstd.dice.model.gamerule.InitJackpotChild;
import com.io.begstd.dice.model.gamerule.RTPDiceConfig;
import com.io.begstd.dice.command.DenominationLevelCmd;
import com.io.begstd.dice.command.ExtraBetLevelCmd;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public interface IDiceMachineConfig extends ICommonDiceConfig {

    Map<String, Money> getJACKPOTS();

    List<String> wonRulesName();
    List<WonRuleExtension> wonRules();
    String serviceId();

    int jackpotLineSize();
    List<InitJackpotChild> initJackpotList();

    ExtraBetLevelCmd getExtraBetLevelById(String id);
    List<DenominationLevel> getDenominationLevels();
    List<DenominationLevel> getDenominationLevelsForBet(String currency);
    int totalCredit();
    int totalPercent();

    CurrencyType getAvailableCurrency(String inputCurrency);
    List<CurrencyType> currencyType();
    Map<String, Money> getJACKPOTSByCurrency(String currency);
    boolean isValidBet(String betId, String currency);
    
    default List<BigWinInfo> getBigWinInfos(){
        return null;
    }

    default String serviceCode() {
        throw new UnsupportedOperationException("Incorrect load config from " + this.getClass().getSimpleName());
    }
    default String serviceName() {
        throw new UnsupportedOperationException("Incorrect load config from " + this.getClass().getSimpleName());
    }
    default String prefixService() {
        throw new UnsupportedOperationException("Incorrect load config from " + this.getClass().getSimpleName());
    }
    default String rtpDefault() {
        throw new UnsupportedOperationException("Incorrect load config from " + this.getClass().getSimpleName());
    }
    default List<RTPDiceConfig> rtpDiceConifgs() {
        throw new UnsupportedOperationException("Incorrect load config from " + this.getClass().getSimpleName());
    }
    default int intervalGame() {
        throw new UnsupportedOperationException("Incorrect load config from " + this.getClass().getSimpleName());
    }
    default double initTrialWalet() {
        throw new UnsupportedOperationException("Incorrect load config from " + this.getClass().getSimpleName());
    }
    default List<InitJackpotChild> initTrialJackpotList() {
        throw new UnsupportedOperationException("Incorrect load config from " + this.getClass().getSimpleName());
    }
    default Map<String, Money> getJackpotListForTrial() {
        throw new UnsupportedOperationException("Incorrect load config from " + this.getClass().getSimpleName());
    }
    default Map<String, Money> JACKPOTS() {
        throw new UnsupportedOperationException("Incorrect load config from " + this.getClass().getSimpleName());
    }
    default List<ExtraBetLevelCmd> extraBetLevels() {
        throw new UnsupportedOperationException("Incorrect load config from " + this.getClass().getSimpleName());
    }
    
    default List<String> getDenominationLevelsForEnv(int env, String currency) {
        throw new UnsupportedOperationException("Incorrect load config from " + this.getClass().getSimpleName());
    }
    default List<String> getExtraBetForEnv() {
        throw new UnsupportedOperationException("Incorrect load config from " + this.getClass().getSimpleName());
    }
    default String stateType() {
        throw new UnsupportedOperationException("Incorrect load config from " + this.getClass().getSimpleName());
    }

    default List<DenominationLevelCmd> denominationLevels() {
        return Collections.EMPTY_LIST;
    }
}
