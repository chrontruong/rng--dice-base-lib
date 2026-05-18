package com.io.begstd.slot.model.app;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.io.begstd.slot.command.ExtraBetLevelCmd;
import com.io.begstd.slot.model.domain.Money;
import com.io.begstd.slot.model.gamerule.BettingLine;
import com.io.begstd.slot.model.gamerule.DataCell;
import com.io.begstd.slot.model.gamerule.DenominationLevel;
import com.io.begstd.slot.model.gamerule.SymbolGamble;
import com.io.begstd.slot.model.playsession.IFreeGameProb;
import com.io.begstd.slot.model.playsession.ISymbol;
import com.io.begstd.slot.model.playsession.ISymbolBonusGame;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This is play session to contain username, freespin times, minigame times,
 * packaging to send
 */
@Getter
@Accessors(fluent = true)
@SuperBuilder(toBuilder = true)
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class BasePlaySession {
    private String uuid;
    private String userId;
    private String userType;
    private String agentWallet;
    private String displayName;
    private String serviceId;
    private String commandId;
    private int version;
    private int env;
    private String ssid;
    private Money totalBet;
    private Money gambleBet;

    private DenominationLevel betDenom;
    private ExtraBetLevelCmd extraDenom;
    private String currency;
    private String lang;

    private Money winAmount;
    private Money winJackpotAmount;
    private DataCell<ISymbol>[][] normalGameMatrix; // latest non null matrix in listNormalGameTransformMatrix
    private List<Integer> normalGameTableFormat = new ArrayList<>();

    private List<DataCell<ISymbol>[][]> listNormalGameTransformMatrix = new ArrayList<>(); // [original matrix, transform 1, transform 2,
    // .. transform n]
    private List<String> normalGamePayLines = new ArrayList<>();

    private Money normalGameWinAmount;
    // store matrix transform for normal game.

    private int freeGameRemain;

    private int freeGameTotal;

    private DataCell<ISymbol>[][] freeGameMatrix; //latest non null matrix in listFreeGameTransformMatrix

    private List<Integer> freeGameTableFormat = new ArrayList<>();

    private List<DataCell<ISymbol>[][]> listFreeGameTransformMatrix = new ArrayList<>(); //[original matrix, transform 1, transform 2,..
    // transform n]

    private List<String> freeGamePayLines = new ArrayList<>();

    private Money freeGameWinAmount;

    private IFreeGameProb freeSpinProb;

    private int bonusGameRemain;

    private int bonusGameTotal;

    private int bonusPlayRemain;

    private DataCell<ISymbolBonusGame>[][] bonusGameMatrix;

    private List<Integer> bonusGameTableFormat = new ArrayList<>();

    private Money bonusGameWinAmount;

    private Money bonusGameWinAmtCurrent;

 // respin game
    private int respinRemain; // use in case respin
    private int respinTotal; // use in case respin
    private DataCell<ISymbol>[][] respinGameMatrix; //latest non null matrix in listRespinGameTransformMatrix
    private List<Integer> respinGameTableFormat = new ArrayList<>();
    private List<DataCell<ISymbol>[][]> listRespinGameTransformMatrix = new ArrayList<>(); //[original matrix, transform 1, transform 2,..
    // transform n]
    private List<String> respinGamePayLines = new ArrayList<>();
    private Money respinGameWinAmount = Money.ZERO;

    
    private boolean isFinished; // set status for play session

    private int state;

    private int exState;

    private List<BettingLine> bettingLines = new ArrayList<>();

    // support for won Jackpot feature
    private ISymbolBonusGame winSymbolBonusGame;
    private List<ISymbolBonusGame> baseReelBonusGameRemain = new ArrayList<>();
    private ISymbolBonusGame latestSymbolBonusGameOpend;

    // store number new free game & bonus game, free game amount,.. user has just
    // win in current spin.
    private int latestWinFreeGameCount;
    private int latestWinBonusGameCount;
//    @JsonProperty("lWFAmt")
//    private Money latestWinFreeGameAmount;
//    @JsonProperty("lWBAmt")
//    private Money latestWinBonusAmount; // bonus

    private List<Integer> freeGameOption = new ArrayList<>();

    // Promotion infor
    private int promotionRemain;
    private int promotionTotal;
    private String promotionCode;
    private String promotionBetId;

    //error code in lastevent
    private List<String> errorCodeList = new ArrayList<>();
    //new for history
    private long startTime; // create time of playsession
    private long lastModified; // create time of playsession

    private int lightningGameRemain;
    private int lightningGameTotal;
    private DataCell<ISymbol>[][] lightningGameMatrix; //latest non null matrix in listFreeGameTransformMatrix

    private List<Integer> lightningGameTableFormat = new ArrayList<>();
    private List<DataCell<ISymbol>[][]> listLightningGameTransformMatrix = new ArrayList<>(); //[original matrix, transform 1, transform
    // 2,.. transform n]

    private List<String> lightningGamePayLines = new ArrayList<>();
    private Money lightningGameWinAmount;
    private int latestWinLightningGameCount;
//    private Money latestWinLightningGameAmount;

    private int powerUpGameRemain;
    private int powerUpGameTotal;
    private int latestWinPowerUpGameCount;

    private boolean isTrialMode;
    private String ticketIdForWallet;

    private String jpInfoAmt; //support BO

    private List<JackpotInfo> jackpotHistory = new ArrayList<>();

    private int jackpotTotalCount;

    private List<String> lastJackpotInfo = new ArrayList<>(); // 1 phan tu: jpId;jpAmount[;jpLineID] -

    private Money latestWinJackpotAmount;

    private Money latestWinAmount; // use for all case: line, scatter, khong tinh jackpot

    //field for gamble
    private long savedTimeOfPlaySession = 0;
    private long expiredTime = 0;
    private boolean isFinishGamble = false;
    private int gambleCount = 0;
    private int gambleGameRemain;
    private int gambleGameTotal;
    private SymbolGamble gamblerUserSymbol;
    private SymbolGamble gamblerSystemSymbol;
    private Money gamblerUserBet = Money.ZERO;
    private int walletOption = 0;

    private String avatar;

    @JsonIgnore
    private String ip;

    public boolean checkFinish() {
        return (this.freeGameRemain() == 0) &&
                (this.bonusGameRemain() == 0) &&
                (this.freeGameOption() == null || this.freeGameOption().isEmpty()) &&
                (this.lightningGameRemain() == 0) &&
                (this.powerUpGameRemain() == 0) &&
                (this.respinRemain == 0);
    }
    
    public boolean hasRespin() {
        return this.respinRemain > 0;
    }
    public boolean hasFreeGame() {
        return this.freeGameRemain > 0;
    }

    public boolean hasBonusGame() {
        return this.bonusGameRemain > 0;
    }

    public boolean hasLightningGame() {
        return this.lightningGameRemain > 0;
    }

    public boolean hasPowerUpGame() {
        return this.powerUpGameRemain > 0;
    }
    // TODO: override at game when it has substate for wonrule in respin
    public BigWinInfo getBigWin(List<BigWinInfo> bigWinInfos) {
        if (!CollectionUtils.isEmpty(bigWinInfos)) {
            List<BigWinInfo> winInfos = bigWinInfos.stream()
                    .sorted(Comparator.comparing(BigWinInfo::level).reversed())
                    .collect(Collectors.toList());
            for (BigWinInfo winInfo : winInfos) {
                switch (winInfo.state()) {
                    case CURRENT:
                        if (latestWinAmount.gte(totalBet.multiply(winInfo.level()))) {
                            return winInfo;
                        }
                        break;
                    case END:
                        if (isFinished && winAmount.gte(totalBet.multiply(winInfo.level()))) {
                            return winInfo;
                        }
                        break;
                }
            }
        }
        return null;
    }

    public abstract static class BasePlaySessionBuilder<C extends BasePlaySession, B extends BasePlaySessionBuilder<C, B>> {
        // Default value.
        private Money winAmount = Money.ZERO;
        private Money normalGameWinAmount = Money.ZERO;
        private Money respinGameWinAmount = Money.ZERO;
        private Money freeGameWinAmount = Money.ZERO;
        private Money bonusGameWinAmount = Money.ZERO;
        private Money winJackpotAmount = Money.ZERO;
        //        private Money latestWinFreeGameAmount = Money.ZERO;
        private Money latestWinJackpotAmount = Money.ZERO;
        //        private Money latestWinBonusAmount = Money.ZERO;
        private Money bonusGameWinAmtCurrent = Money.ZERO;

        private Money latestWinAmount = Money.ZERO;
        private List<String> normalGamePayLines = new ArrayList<>();
        private List<String> freeGamePayLines = new ArrayList<>();
        private List<Integer> freeGameOption;

        private List<JackpotInfo> jackpotHistory = new ArrayList<JackpotInfo>();

        private Money latestWinLightningGameAmount = Money.ZERO;
        private Money lightningGameWinAmount = Money.ZERO;

        //        private List<String> jackpotInfo = new ArrayList<>();
        private List<String> lastJackpotInfo = new ArrayList<>();


        // Support function.
        public BasePlaySessionBuilder addNormalGameWinAmount(Money winAmount) {
            this.normalGameWinAmount = this.normalGameWinAmount.add(winAmount);
            this.winAmount = this.winAmount.add(winAmount);
            this.latestWinAmount = winAmount;
            return this;
        }

        public BasePlaySessionBuilder addReSpinGameWinAmount(Money winAmount) {
            this.respinGameWinAmount = this.respinGameWinAmount.add(winAmount);
            this.winAmount = this.winAmount.add(winAmount);
            this.latestWinAmount = winAmount;
            return this;
        }

        public BasePlaySessionBuilder addFreeGameWinAmount(Money winAmount) {
//            this.latestWinFreeGameAmount = winAmount;
            this.latestWinAmount = winAmount;
            this.freeGameWinAmount = this.freeGameWinAmount.add(winAmount);
            this.winAmount = this.winAmount.add(winAmount);
            return this;
        }

        public BasePlaySessionBuilder addFreeGameWinAmountWithLatest(Money winAmount) {
//            this.latestWinFreeGameAmount = this.latestWinFreeGameAmount.add(winAmount);
            this.latestWinAmount = this.latestWinAmount.add(winAmount);
            this.freeGameWinAmount = this.freeGameWinAmount.add(winAmount);
            this.winAmount = this.winAmount.add(winAmount);
            return this;
        }


        public BasePlaySessionBuilder addBonusGameWinAmount(Money winAmount) {
            this.bonusGameWinAmount = this.bonusGameWinAmount.add(winAmount);
            if (this.bonusGameWinAmtCurrent == null) {
                this.bonusGameWinAmtCurrent = Money.ZERO;
            }
            this.bonusGameWinAmtCurrent = this.bonusGameWinAmtCurrent.add(winAmount);
//            this.latestWinBonusAmount = winAmount;
            this.latestWinAmount = winAmount;
            this.winAmount = this.winAmount.add(winAmount);
            return this;
        }

        public BasePlaySessionBuilder addJackpotWinAmount(Money money) {
            this.winJackpotAmount = this.winJackpotAmount.add(money);
            this.winAmount = this.winAmount.add(money);
            this.latestWinJackpotAmount = money;

            return this;
        }

        public BasePlaySessionBuilder addJackpotInfo(String jpId, Money money, String jpLine) {
            StringBuffer strB = new StringBuffer();
            strB.append(jpId).append(";").append(money.value().doubleValue());
            if (!StringUtils.isEmpty(jpLine)) {
                strB.append(";").append(jpLine);
            }
            this.lastJackpotInfo.add(strB.toString());
//            this.jackpotInfo.add(strB.toString());
            return this;
        }


        public BasePlaySessionBuilder addJackpotWinAmountWithLatest(Money money) {
            this.winJackpotAmount = this.winJackpotAmount.add(money);
            this.winAmount = this.winAmount.add(money);
            this.latestWinJackpotAmount = this.latestWinJackpotAmount.add(money);
//            latestWinAmount never includes jackpot
//            this.latestWinAmount = this.latestWinAmount.add(money);

            return this;
        }

        public BasePlaySessionBuilder addWinAmount(Money money) {
            this.winAmount = this.winAmount.add(money);
            return this;
        }

        public BasePlaySessionBuilder decreaseFreeGameRemain() {
            this.freeGameRemain--;
            return this;
        }

        public BasePlaySessionBuilder decreaseReSpinGameRemain() {
            this.respinRemain--;
            return this;
        }

        public BasePlaySessionBuilder increaseVersion() {
            this.version++;
            return this;
        }

        public BasePlaySessionBuilder addFreeGameCount(int freeGames) {
            this.latestWinFreeGameCount += freeGames;
            this.freeGameRemain += freeGames;
            this.freeGameTotal += freeGames;
            return this;
        }

        public BasePlaySessionBuilder addReSpinGameCount(int reGames) {
            //this.latestWinFreeGameCount += reGames;
            this.respinRemain += reGames;
            this.respinTotal += reGames;
            return this;
        }

        public BasePlaySessionBuilder addBonusGameCount(int bonusGame) {
            this.latestWinBonusGameCount += bonusGame;
            this.bonusGameRemain += bonusGame;
            this.bonusGameTotal += bonusGame;
            return this;
        }

        public BasePlaySessionBuilder resetNormalBeforeClone() {
            this.latestWinFreeGameCount(0);
//            this.latestWinFreeGameAmount(Money.ZERO);
            this.latestWinAmount(Money.ZERO);
            this.latestWinBonusGameCount(0);
            this.bonusPlayRemain(0);
            this.freeGameTotal(0);
            this.freeGameRemain(0);
            this.bonusGameTotal(0);
            this.bonusGameRemain(0);
            this.lightningGameTotal(0);
            this.lightningGameRemain(0);
//            this.latestWinLightningGameAmount(Money.ZERO);
            this.latestWinLightningGameCount(0);
            this.latestWinPowerUpGameCount(0);
            this.powerUpGameRemain(0);
            this.powerUpGameTotal(0);
            this.latestWinJackpotAmount(Money.ZERO);
//            this.latestWinBonusAmount(Money.ZERO);

            this.normalGamePayLines(new ArrayList<>());
            this.freeGamePayLines(new ArrayList<>());

            this.normalGameMatrix(null);
            this.listNormalGameTransformMatrix(null);
            this.normalGameWinAmount = Money.ZERO;
            this.normalGameTableFormat(new ArrayList<>());
            return this;
        }

        public BasePlaySessionBuilder resetLastFreeGameWinData() {
            this.latestWinFreeGameCount(0);
            this.latestWinBonusGameCount(0);
            this.latestWinAmount(Money.ZERO);
//            this.latestWinFreeGameAmount(Money.ZERO);
            this.latestWinJackpotAmount(Money.ZERO);
//            this.latestWinBonusAmount(Money.ZERO);
            this.freeGamePayLines(new ArrayList<>());
            this.lastJackpotInfo(new ArrayList<>());
            return this;
        }

        //support for bonus
        public BasePlaySessionBuilder decreaseBonusGameRemain() {
            this.bonusGameRemain--;
            return this;
        }

        public BasePlaySessionBuilder decreaseBonusPlayRemain() {
            this.bonusPlayRemain--;
            return this;
        }

        public BasePlaySessionBuilder resetBonusPlayRemain(int bonusPlayRemain) {
            this.bonusPlayRemain = bonusPlayRemain;
            return this;
        }

        public BasePlaySessionBuilder resetBonusGameRemain(int bonusBonusRemain) {
            this.bonusGameRemain = bonusBonusRemain;
            return this;
        }

        public BasePlaySessionBuilder resetLatestBonusGame() {
//            this.latestWinBonusAmount = Money.ZERO;
            this.latestWinAmount(Money.ZERO);
            this.latestSymbolBonusGameOpend = null;
//            this.latestWinFreeGameAmount(Money.ZERO);
            this.latestWinJackpotAmount(Money.ZERO);
            this.lastJackpotInfo(new ArrayList<>());
            return this;
        }

        public BasePlaySessionBuilder resetBonusWinAmountCurrent() {
            this.bonusGameWinAmtCurrent = Money.ZERO;
            return this;
        }

        public BasePlaySessionBuilder addFreeGameOption(List<Integer> freeGameOption) {
            if (this.freeGameOption == null) {
                this.freeGameOption = new ArrayList<Integer>();
            }
            this.freeGameOption.addAll(freeGameOption);
            return this;
        }

        public BasePlaySessionBuilder clearFreeGameOption() {
            this.freeGameOption = null;
            return this;
        }

        public BasePlaySessionBuilder addNormalGamePayLines(List<String> payline) {
            if (this.normalGamePayLines != null) {
                this.normalGamePayLines.addAll(payline);
            } else {
                this.normalGamePayLines = payline;
            }

            return this;
        }

        public BasePlaySessionBuilder addFreeGamePayLines(List<String> payline) {
            if (this.freeGamePayLines != null) {
                this.freeGamePayLines.addAll(payline);
            } else {
                this.freeGamePayLines = payline;
            }

            return this;
        }

        public BasePlaySessionBuilder addErrorCodeList(String errorCode) {
            if (this.errorCodeList == null) {
                this.errorCodeList = new ArrayList<String>();
            }
            this.errorCodeList.add(errorCode);
            return this;
        }

        public BasePlaySessionBuilder resetErrorCodeList() {
            if (this.errorCodeList != null)
                this.errorCodeList.clear();
            return this;
        }

        public BasePlaySessionBuilder setStartTime() {
            this.startTime = Instant.now().toEpochMilli();
            return this;

        }

        public BasePlaySessionBuilder addJackpotHistory(JackpotInfo jackpotInfo) {
            if (this.jackpotHistory == null) {
                this.jackpotHistory = new ArrayList<JackpotInfo>();
            }
            this.jackpotHistory.add(jackpotInfo);
            return this;
        }
        // End support function.


        public BasePlaySessionBuilder addLightningGameWinAmount(Money winAmount) {
//            this.latestWinLightningGameAmount = winAmount;
            this.latestWinAmount = winAmount;
            this.lightningGameWinAmount = this.lightningGameWinAmount.add(winAmount);
            this.winAmount = this.winAmount.add(winAmount);
            return this;
        }

        public BasePlaySessionBuilder addLightningGameWinAmountWithLatest(Money winAmount) {
//            this.latestWinLightningGameAmount = this.latestWinLightningGameAmount.add(winAmount);
            this.latestWinAmount = latestWinAmount.add(winAmount);
            this.lightningGameWinAmount = this.lightningGameWinAmount.add(winAmount);
            this.winAmount = this.winAmount.add(winAmount);
            return this;
        }

        public BasePlaySessionBuilder addLightningGameCount(int lightningGames) {
            this.latestWinLightningGameCount += lightningGames;
            this.lightningGameRemain += lightningGames;
            this.lightningGameTotal += lightningGames;
            return this;
        }

        public BasePlaySessionBuilder addLightningGamePayLines(List<String> payline) {
            if (this.lightningGamePayLines != null) {
                this.lightningGamePayLines.addAll(payline);
            } else {
                this.lightningGamePayLines = payline;
            }
            return this;
        }

        public BasePlaySessionBuilder resetLastLightningGameWinData() {
            this.latestWinLightningGameCount(0);
            this.latestWinAmount(Money.ZERO);
//            this.latestWinLightningGameAmount(Money.ZERO);
            this.lightningGamePayLines(new ArrayList<>());
            return this;
        }

        public BasePlaySessionBuilder decreaseLightningGameRemain() {
            this.lightningGameRemain--;
            return this;
        }

        public BasePlaySessionBuilder decreasePowerUpGameRemain() {
            this.powerUpGameRemain--;
            return this;
        }

        public BasePlaySessionBuilder resetLastPowerUpGameWinData() {
            this.latestWinPowerUpGameCount(0);
            this.latestWinBonusGameCount(0);
            return this;
        }

        public BasePlaySessionBuilder addPowerUpGameCount(int powerUpGameCount) {
            this.powerUpGameRemain += powerUpGameCount;
            this.powerUpGameTotal += powerUpGameCount;
            this.latestWinPowerUpGameCount += powerUpGameCount;
            return this;
        }

        //gamble mode
        public BasePlaySessionBuilder addGambleGameCount(int freeGames) {
            this.gambleGameTotal += freeGames;
            this.gambleGameRemain += freeGames;
            return this;
        }

        public BasePlaySessionBuilder decreaseGambleGameRemain() {
            this.gambleGameRemain--;
            return this;
        }

        public BasePlaySessionBuilder increaseGambleGameLevel() {
            this.gambleCount++;
            return this;
        }

        // End support function.
    }
}
