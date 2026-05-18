package com.io.begstd.slot.model.config;

import com.io.begstd.slot.command.DenominationLevelCmd;
import com.io.begstd.slot.command.ExtraBetLevelCmd;
import com.io.begstd.slot.machine.wonrule.WonRuleExtension;
import com.io.begstd.slot.model.app.BigWinInfo;
import com.io.begstd.slot.model.domain.Money;
import com.io.begstd.slot.model.gamerule.*;
import com.io.begstd.slot.model.playsession.ISymbol;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public interface ISlotMachineConfig extends ICommonSlotConfig {
    int reelSize();
    int rowSize();

    Map<String, Money> getJACKPOTS();
    List<Integer> tableFormat();

    String defineMatrixFormatRule();
    List<String> listPreGenerateMatrixRule();
    List<ReelGenerateConfig> listReelConfig();
    List<TransformMatrixConfig> listTransformMatrixConfig();

    Symbol[][] baseSymbolReel(); // default rtp98
    Symbol getSymbolByType(SymbolType type);

    List<String> wonRulesName();
    List<WonRuleExtension> wonRules();
    String serviceId();

    int jackpotLineSize();
    List<InitJackpotChild> initJackpotList();
    int totalPercent();

    ExtraBetLevelCmd getExtraBetLevelById(String id);

    int numPlayTotalInBonusGame();

    int numSymbolWinFreeGame();
    int numSymbolWinBonusGame();
    List<Integer> reelCheckForFreeGame();
    List<Integer> reelCheckForBonusGame();
    Symbol[] getSymbolsForTest(String[] list);
    List<DenominationLevel> getDenominationLevels();
    List<DenominationLevel> getDenominationLevelsForBet(String currency);
    List<PayLine> payLines();
    List<Integer> reelCheckForLightningGame();
    int numSymbolWinLightningGame();
    int numPlayTotalInLightningGame();
    List<ISymbol> rightCheckPayLineSymbol();
    List<String> getLightningSymbolCodes();
    List<Integer> reelCheckForPowerUpGame();
    int totalCredit();
    Symbol[][] baseSymbolReelTrial();

    CurrencyType getAvailableCurrency(String inputCurrency);   
    List<CurrencyType> currencyType();      
    Map<String, Money> getJACKPOTSByCurrency(String currency);
    boolean isValidBet(String betId, String currency);
    
    default List<BigWinInfo> getBigWinInfos(){
        return null;
    }

    boolean isWinFullLine();
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
    default List<RTPSlotConfig> rtpSlotConifgs() {
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
    default <T extends FreeSpinOption> List<T> freeSpinOption() {
        throw new UnsupportedOperationException("Incorrect load config from " + this.getClass().getSimpleName());
    }
    default <T extends Symbol> List<T> symbols() {
        throw new UnsupportedOperationException("Incorrect load config from " + this.getClass().getSimpleName());
    }
    default List<Integer> getFreeSpinOptin() {
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
