package com.io.begstd.dice.model.app;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.io.begstd.dice.model.domain.Money;
import com.io.begstd.dice.model.gamerule.DenominationLevel;
import com.io.begstd.dice.command.ExtraBetLevelCmd;
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

    private Money normalGameWinAmount;

    
    private boolean isFinished; // set status for play session

    private int state;

    private int exState;

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

    private boolean isTrialMode;
    private String ticketIdForWallet;

    private String jpInfoAmt; //support BO

    private List<JackpotInfo> jackpotHistory = new ArrayList<>();

    private int jackpotTotalCount;

    private List<String> lastJackpotInfo = new ArrayList<>(); // 1 phan tu: jpId;jpAmount[;jpLineID] -

    private Money latestWinJackpotAmount;

    private Money latestWinAmount; // use for all case: line, scatter, khong tinh jackpot

    //field for gamble
    private int walletOption = 0;

    private String avatar;

    @JsonIgnore
    private String ip;

    public boolean checkFinish() {
        return true;
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
        private Money winJackpotAmount = Money.ZERO;
        private Money latestWinJackpotAmount = Money.ZERO;

        private Money latestWinAmount = Money.ZERO;
        private List<String> normalGamePayLines = new ArrayList<>();
        private List<String> freeGamePayLines = new ArrayList<>();
        private List<Integer> freeGameOption;

        private List<JackpotInfo> jackpotHistory = new ArrayList<>();

        private List<String> lastJackpotInfo = new ArrayList<>();


        // Support function.
        public BasePlaySessionBuilder addNormalGameWinAmount(Money winAmount) {
            this.normalGameWinAmount = this.normalGameWinAmount.add(winAmount);
            this.winAmount = this.winAmount.add(winAmount);
            this.latestWinAmount = winAmount;
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

        public BasePlaySessionBuilder increaseVersion() {
            this.version++;
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
    }
}
