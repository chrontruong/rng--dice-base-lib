package com.io.begstd.dice.rtp.model;

import com.io.begstd.dice.model.config.DiceMachineConfigForNormal;
import com.io.begstd.dice.model.domain.Money;
import com.io.begstd.dice.model.gamerule.DenominationLevel;
import com.io.begstd.dice.model.gamerule.InitJackpotChild;
import com.io.begstd.dice.services.external.JackpotService;
import com.io.begstd.dice.utils.BeanUtils;
import com.io.begstd.dice.common.DiceGameConstant;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

@Getter
@NoArgsConstructor(access = AccessLevel.PUBLIC)
public class RtpResult {
    
    //Total win value for all spin
    @Setter
    private double betAmount = 0;
    
    @Setter
    private String betId = "";
    @Setter
    private String currency = DiceGameConstant.CURRENCY_DEFAULT;
    //Total win value for all spin
    private double totalWin = 0;

    //Total bet value for all spin
    private double totalBet = 0;

    //Total money win by jackpot win
    private double totalJackPotWin = 0;

    //Total money win by mini win
    private double totalMiniWin = 0;
    //Total extra minigame spin win in the whole test.
    private long numberMiniWin = 0;

    //Number extra freegame spin win in the whole test.
  //Total money win by free win
    private double totalFreeSpinWin = 0;
    private long numberFreeSpinWin = 0;
    private long numberFreeSpinCount = 0;

    // count join from
    private long countNormalToBonusSpin = 0;
    private long countNormalToFreeSpin = 0;
    private long countFreeToBonusSpin = 0;
    private long countFreeToFreeSpin = 0;
    private long countWinAmountGreaterBet = 0;
    //Total extra jackpot spin win in the whole test.
    private long numberJackpotWin = 0;
    
    private double totalJackpotWinMini = 0.0;    
    private long numberJackpotWinMini = 0; 
    private double totalJackpotWinMinor = 0.0;
    private long numberJackpotWinMinor = 0; 
    private double totalJackpotWinMajor = 0.0;
    private long numberJackpotWinMajor = 0; 
    private double totalJackpotWinGrand = 0.0;
    private long numberJackpotWinGrand = 0; 
    private double totalNormalWin = 0.0;
    private long numberNormalWin = 0;
    private long numberSpin = 0;
    
    private long numberHasK;
    
    private Map<String, RtpSymbolWin> payLinesForNormalSpin = new HashMap<String, RtpSymbolWin>();
    private Map<String, RtpSymbolWin> payLinesForFreeSpin = new HashMap<String, RtpSymbolWin>();
    
    private double totalNormalBigWin = 0.0;
    private double totalFreeBigWin = 0.0;
    private double totalBonusForNormalBigWin = 0.0;
    private double totalBonusForFreeBigWin = 0.0;
    
    private long numberNormalBigWin = 0;
    private long numberFreeBigWin = 0;
    private long numberBonusForNormalBigWin = 0;
    private long numberBonusForFreeBigWin = 0;
    
    private double totalNormalMegaBigWin = 0.0;
    private double totalFreeMegaBigWin = 0.0;
    private double totalBonusForNormalMegaBigWin = 0.0;
    private double totalBonusForFreeMegaBigWin = 0.0;
    
    private double totalNormalFortuneBigWin = 0.0;
    private double totalFreeFortuneBigWin = 0.0;
    private double totalBonusForNormalFortuneBigWin = 0.0;
    private double totalBonusForFreeFortuneBigWin = 0.0;
    
    private long numberNormalMegaBigWin = 0;
    private long numberFreeMegaBigWin = 0;
    private long numberBonusForNormalMegaBigWin = 0;
    private long numberBonusForFreeMegaBigWin = 0;
    
    private long numberNormalFortuneBigWin = 0;
    private long numberFreeFortuneBigWin = 0;
    private long numberBonusForNormalFortuneBigWin = 0;
    private long numberBonusForFreeFortuneBigWin = 0;
    // count win rate
    private double totalWinPSFree;
    private double totalWinPSBonus;
    private int countPSWinFree;
    private int countPSWinBonus;
    private int countDrawTicket;
    private Map<Integer, Integer> countWinRate = new HashMap<>();
    //add new
    private double totalBaseFreeAmount = 0.0;
    private int numberBaseFreeWin = 0;
    private double totalBaseNormalAmount = 0.0;
    private int numberBaseNormalWin = 0;

    // bigwin (from inclusive to exclusive)
    private double totalBigWinFromGT0To1 = 0.0D; // > 1 and < 5
    private double totalBigWinFromGT1To5 = 0.0D; // > 1 and < 5
    private double totalBigWinFrom5To10 = 0.0D;
    private double totalBigWinFrom10To15 = 0.0D;
    private double totalBigWinFrom15To20 = 0.0D;
    private double totalBigWinFrom20To25 = 0.0D;
    private double totalBigWinFrom25To30 = 0.0D;
    private double totalBigWinFrom30To35 = 0.0D;
    private double totalBigWinFrom35To40 = 0.0D;
    private double totalBigWinFrom40To45 = 0.0D;
    private double totalBigWinFrom45To50 = 0.0D;
    private double totalBigWinFrom50Upto = 0.0D;
    private long numberBigWinFromGT0To1 = 0;
    private long numberBigWinFromGT1To5 = 0;
    private long numberBigWinFrom5To10 = 0;
    private long numberBigWinFrom10To15 = 0;
    private long numberBigWinFrom15To20 = 0;
    private long numberBigWinFrom20To25 = 0;
    private long numberBigWinFrom25To30 = 0;
    private long numberBigWinFrom30To35 = 0;
    private long numberBigWinFrom35To40 = 0;
    private long numberBigWinFrom40To45 = 0;
    private long numberBigWinFrom45To50 = 0;
    private long numberBigWinFrom50Upto = 0;

    private double totalBigWinFrom50To55 = 0.0D;
    private double totalBigWinFrom55To60 = 0.0D;
    private double totalBigWinFrom60To65 = 0.0D;
    private double totalBigWinFrom65To70 = 0.0D;
    private double totalBigWinFrom70To75 = 0.0D;
    private double totalBigWinFrom75To80 = 0.0D;
    private double totalBigWinFrom80To85 = 0.0D;
    private double totalBigWinFrom85To90 = 0.0D;
    private double totalBigWinFrom90To95 = 0.0D;
    private double totalBigWinFrom95To100 = 0.0D;

    private long numberBigWinFrom50To55 = 0;
    private long numberBigWinFrom55To60 = 0;
    private long numberBigWinFrom60To65 = 0;
    private long numberBigWinFrom65To70 = 0;
    private long numberBigWinFrom70To75 = 0;
    private long numberBigWinFrom75To80 = 0;
    private long numberBigWinFrom80To85 = 0;
    private long numberBigWinFrom85To90 = 0;
    private long numberBigWinFrom90To95 = 0;
    private long numberBigWinFrom95To100 = 0;
    

    public void addTotalBigWinFromGT0To1(double amt) {
        this.totalBigWinFromGT0To1 += amt;
    }
    
    public void addTotalBigWinFromGT1To5(double amt) {
        this.totalBigWinFromGT1To5 += amt;
    }
    public void addTotalBigWinFrom5To10(double amt) {
        this.totalBigWinFrom5To10 += amt;
    }
    public void addTotalBigWinFrom10To15(double amt) {
        this.totalBigWinFrom10To15 += amt;
    }
    public void addTotalBigWinFrom15To20(double amt) {
        this.totalBigWinFrom15To20 += amt;
    }
    public void addTotalBigWinFrom20To25(double amt) {
        this.totalBigWinFrom20To25 += amt;
    }
    public void addTotalBigWinFrom25To30(double amt) {
        this.totalBigWinFrom25To30 += amt;
    }
    public void addTotalBigWinFrom30To35(double amt) {
        this.totalBigWinFrom30To35 += amt;
    }
    public void addTotalBigWinFrom35To40(double amt) {
        this.totalBigWinFrom35To40 += amt;
    }
    public void addTotalBigWinFrom40To45(double amt) {
        this.totalBigWinFrom40To45 += amt;
    }
    public void addTotalBigWinFrom45To50(double amt) {
        this.totalBigWinFrom45To50 += amt;
    }
    public void addTotalBigWinFrom50Upto(double amt) {
        this.totalBigWinFrom50Upto += amt;
    }

    public void increaseNumberBigWinFromGT0To1() {
        this.numberBigWinFromGT0To1++;
    }
    public void increaseNumberBigWinFromGT1To5() {
        this.numberBigWinFromGT1To5++;
    }
    public void increaseNumberBigWinFrom5To10() {
        this.numberBigWinFrom5To10++;
    }
    public void increaseNumberBigWinFrom10To15() {
        this.numberBigWinFrom10To15++;
    }
    public void increaseNumberBigWinFrom15To20() {
        this.numberBigWinFrom15To20++;
    }
    public void increaseNumberBigWinFrom20To25() {
        this.numberBigWinFrom20To25++;
    }
    public void increaseNumberBigWinFrom25To30() {
        this.numberBigWinFrom25To30++;
    }
    public void increaseNumberBigWinFrom30To35() {
        this.numberBigWinFrom30To35++;
    }
    public void increaseNumberBigWinFrom35To40() {
        this.numberBigWinFrom35To40++;
    }
    public void increaseNumberBigWinFrom40To45() {
        this.numberBigWinFrom40To45++;
    }
    public void increaseNumberBigWinFrom45To50() {
        this.numberBigWinFrom45To50++;
    }
    public void increaseNumberBigWinFrom50Upto() {
        this.numberBigWinFrom50Upto++;
    }


    public void addTotalBigWinFrom50To55(double totalBigWinFrom50To55) {
        this.totalBigWinFrom50To55 += totalBigWinFrom50To55;
    }
    public void addTotalBigWinFrom55To60(double totalBigWinFrom55To60) {
        this.totalBigWinFrom55To60 += totalBigWinFrom55To60;
    }
    public void addTotalBigWinFrom60To65(double totalBigWinFrom60To65) {
        this.totalBigWinFrom60To65 += totalBigWinFrom60To65;
    }
    public void addTotalBigWinFrom65To70(double totalBigWinFrom65To70) {
        this.totalBigWinFrom65To70 += totalBigWinFrom65To70;
    }
    public void addTotalBigWinFrom70To75(double totalBigWinFrom70To75) {
        this.totalBigWinFrom70To75 += totalBigWinFrom70To75;
    }
    public void addTotalBigWinFrom75To80(double totalBigWinFrom75To80) {
        this.totalBigWinFrom75To80 += totalBigWinFrom75To80;
    }
    public void addTotalBigWinFrom80To85(double totalBigWinFrom80To85) {
        this.totalBigWinFrom80To85 += totalBigWinFrom80To85;
    }
    public void addTotalBigWinFrom85To90(double totalBigWinFrom85To90) {
        this.totalBigWinFrom85To90 += totalBigWinFrom85To90;
    }
    public void addTotalBigWinFrom90To95(double totalBigWinFrom90To95) {
        this.totalBigWinFrom90To95 += totalBigWinFrom90To95;
    }
    public void addTotalBigWinFrom95To100(double totalBigWinFrom95To100) {
        this.totalBigWinFrom95To100 += totalBigWinFrom95To100;
    }

    public void increaseNumberBigWinFrom50To55() {
        this.numberBigWinFrom50To55++;
    }
    public void increaseNumberBigWinFrom55To60() {
        this.numberBigWinFrom55To60++;
    }
    public void increaseNumberBigWinFrom60To65() {
        this.numberBigWinFrom60To65++;
    }
    public void increaseNumberBigWinFrom65To70() {
        this.numberBigWinFrom65To70++;
    }
    public void increaseNumberBigWinFrom70To75() {
        this.numberBigWinFrom70To75++;
    }
    public void increaseNumberBigWinFrom75To80() {
        this.numberBigWinFrom75To80++;
    }
    public void increaseNumberBigWinFrom80To85() {
        this.numberBigWinFrom80To85++;
    }
    public void increaseNumberBigWinFrom85To90() {
        this.numberBigWinFrom85To90++;
    }
    public void increaseNumberBigWinFrom90To95() {
        this.numberBigWinFrom90To95++;
    }
    public void increaseNumberBigWinFrom95To100() {
        this.numberBigWinFrom95To100++;
    }

    public void increaseCountDrawTicket() {
        this.countDrawTicket++;
    }

    public void increaseCountPSWinFree() {
        this.countPSWinFree++;
    }
    
    public void increaseCountPSWinBonus() {
        this.countPSWinBonus++;
    }
    
    public void addTotalWinPSFree(double winAmount) {
        this.totalWinPSFree += winAmount;
    }
    
    public void addTotalWinPSBonus(double winAmount) {
        this.totalWinPSBonus += winAmount;
    }
    
    public void addTotalWin(double totalWin) {
        this.totalWin += totalWin;
    }

    public void addTotalBet(double totalBet) {
        this.totalBet += totalBet;
    }

    public void addTotalJackpotWin(double totalJackPotWin) {
        this.totalJackPotWin += totalJackPotWin;
    }

    public void addTotalMiniWin(double totalMiniWin) {
        this.totalMiniWin += totalMiniWin;
    }

    public void addTotalFreeSpinWin(double totalFreeSpinWin) {
        this.totalFreeSpinWin += totalFreeSpinWin;
    }
    
    public void addTotalJackpotWinMini(double totalJackpotWinMini) {
        this.totalJackpotWinMini += totalJackpotWinMini;
    }
    
    public void addTotalJackpotWinMinor(double totalJackpotWinMinor) {
        this.totalJackpotWinMinor += totalJackpotWinMinor;
    }

    public void addTotalJackpotWinMajor(double totalJackpotWinMajor) {
        this.totalJackpotWinMajor += totalJackpotWinMajor;
    }
    
    public void addTotalJackpotWinGrand(double totalJackpotWinGrand) {
        this.totalJackpotWinGrand += totalJackpotWinGrand;
    }

    public void addTotalNormalWin(double totalNormalWin) {
        this.totalNormalWin += totalNormalWin;
    }
    
    public void increaseNormalWin() {
        this.numberNormalWin++;
    }
    
    public void increaseNumberSpin() {
        this.numberSpin++;
    }
    
    public void increaseNumberHasK() {
        this.numberHasK++;
    }
    
    public void increaseFreeSpinWin() {
        this.numberFreeSpinWin++;
        this.numberFreeSpinCount++;
    }
    
    public void increaseNumberFreeSpin() {
        this.numberFreeSpinCount++;
    }
    
    public void increaseJackpotWin() {
        this.numberJackpotWin++;
    }
    
    public void increaseMiniWin() {
        this.numberMiniWin++;
    }
    
    public void increaseNumberJackpotWinMini() {
        this.numberJackpotWinMini++;
    }

    public void increaseNumberJackpotWinMinor() {
        this.numberJackpotWinMinor++;
    }
    
    public void increaseNumberJackpotWinMajor() {
        this.numberJackpotWinMajor++;
    }
    
    public void increaseNumberJackpotWinGrand() {
        this.numberJackpotWinGrand++;
    }
    
    public void increaseCountNormalToBonusSpin() {
        this.countNormalToBonusSpin++;
    }
    public void increaseCountNormalToFreeSpin() {
        this.countNormalToFreeSpin++;
    }
    public void increaseCountFreeToBonusSpin() {
        this.countFreeToBonusSpin++;
    }
    public void increaseCountFreeToFreeSpin() {
        this.countFreeToFreeSpin++;
    }
    
    public void increaseCountWinAmountGreaterBet() {
        this.countWinAmountGreaterBet++;
    }
    
    public void putPaylineForNormal(String key, RtpSymbolWin symbolWin) {
        if(payLinesForNormalSpin.containsKey(key)) {
            payLinesForNormalSpin.get(key).addRtpSymbolWin(symbolWin);
        }else {
            payLinesForNormalSpin.put(key, new RtpSymbolWin(symbolWin.getCount(), symbolWin.getPayWin(), key));
        }
    }
    
    public void putPaylineForFree(String key, RtpSymbolWin symbolWin) {
        if(payLinesForFreeSpin.containsKey(key)) {
            payLinesForFreeSpin.get(key).addRtpSymbolWin(symbolWin);
        }else {
            payLinesForFreeSpin.put(key, new RtpSymbolWin(symbolWin.getCount(), symbolWin.getPayWin(), key));
        }
    }
    
    public void addTotalNormalBigWin(double money) {
        this.totalNormalBigWin += money;
    }
    
    public void addTotalFreeBigWin(double money) {
        this.totalFreeBigWin += money;
    }
    
    public void addTotalBonusForNormalBigWin(double money) {
        this.totalBonusForNormalBigWin += money;
    }
    
    public void addTotalBonusForFreeBigWin(double money) {
        this.totalBonusForFreeBigWin += money;
    }
    
    public void increaseNumberNormalBigWin() {
        this.numberNormalBigWin++;
    }

    public void increaseNumberFreeBigWin() {
        this.numberFreeBigWin++;
    }
    
    public void increaseNumberBonusForNormalBigWin() {
        this.numberBonusForNormalBigWin++;
    }
    
    public void increaseNumberBonusForFreeBigWin() {
        this.numberBonusForFreeBigWin++;
    }
    
    public void addTotalNormalMegaBigWin(double money) {
        this.totalNormalMegaBigWin += money;
    }
    
    public void addTotalFreeMegaBigWin(double money) {
        this.totalFreeMegaBigWin += money;
    }
    
    public void addTotalBonusForNormalMegaBigWin(double money) {
        this.totalBonusForNormalMegaBigWin += money;
    }
    
    public void addTotalBonusForFreeMegaBigWin(double money) {
        this.totalBonusForFreeMegaBigWin += money;
    }
    
    public void increaseNumberNormalMegaBigWin() {
        this.numberNormalMegaBigWin++;
    }

    public void increaseNumberFreeMegaBigWin() {
        this.numberFreeMegaBigWin++;
    }
    
    public void increaseNumberBonusForNormalMegaBigWin() {
        this.numberBonusForNormalMegaBigWin++;
    }
    
    public void increaseNumberBonusForFreeMegaBigWin() {
        this.numberBonusForFreeMegaBigWin++;
    }
    
    public void addTotalNormalFortuneBigWin(double money) {
        this.totalNormalFortuneBigWin += money;
    }
    
    public void addTotalFreeFortuneBigWin(double money) {
        this.totalFreeFortuneBigWin += money;
    }
    
    public void addTotalBonusForNormalFortuneBigWin(double money) {
        this.totalBonusForNormalFortuneBigWin += money;
    }
    
    public void addTotalBonusForFreeFortuneBigWin(double money) {
        this.totalBonusForFreeFortuneBigWin += money;
    }
    
    public void increaseNumberNormalFortuneBigWin() {
        this.numberNormalFortuneBigWin++;
    }

    public void increaseNumberFreeFortuneBigWin() {
        this.numberFreeFortuneBigWin++;
    }
    
    public void increaseNumberBonusForNormalFortuneBigWin() {
        this.numberBonusForNormalFortuneBigWin++;
    }
    
    public void increaseNumberBonusForFreeFortuneBigWin() {
        this.numberBonusForFreeFortuneBigWin++;
    }
    
    public void addTotalBaseNormalAmount(double totalBaseNormalAmount) {
        this.totalBaseNormalAmount += totalBaseNormalAmount;
    }
    public void addTotalBaseFreeAmount(double totalBaseFreeAmount) {
        this.totalBaseFreeAmount += totalBaseFreeAmount;
    }
    
    public void increaseNumberBaseNormalWin() {
        this.numberBaseNormalWin++;
    }
    public void increaseNumberBaseFreeWin() {
        this.numberBaseFreeWin++;
    }

    public void putCountWinRate(int key, int countN) {
        if (countWinRate.containsKey(key)) {
            int curCount = countWinRate.get(key);
            countWinRate.put(key, curCount + countN);
        } else {
            countWinRate.put(key, countN);
        }
    }
    public void addWinRate(double winRate) {
        int index = -1;
        //- count win 0: ...
        //- win 0 < x <= 0.25:
        //- win 0.25 < x <= 0.5:
        //- win 0.5 < x <= 0.75:
        //- win 0.75 < x <= 0.9:
        //- win 0.9 < x < 1
        if (winRate == 0) {
            index = 0;
        } else if (winRate <= 0.25) {
            index = 1;
        } else if (winRate <= 0.5) {
            index = 2;
        } else if (winRate <= 0.75) {
            index = 3;
        } else if (winRate <= 0.9) {
            index = 4;
        } else if (winRate < 1) {
            index = 5;
        } else if (winRate == 1) {
            index = 6;
        } else if (winRate > 1 && winRate <= 1.25) {
            index = 7;
        } else if (winRate > 1.25 && winRate <= 1.5) {
            index = 8;
        } else if (winRate > 1.5 && winRate <= 1.75) {
            index = 9;
        } else if (winRate > 1.75 && winRate < 2) {
            index = 10;
        } else if (winRate == 2) {
            index = 11;
        } else if (winRate > 2 && winRate < 3) {
            index = 12;
        } else if (winRate == 3) {
            index = 13;
        } else if (winRate > 3 && winRate < 5) {
            index = 14;
        } else if (winRate == 5) {
            index = 15;
        } else if (winRate > 5 && winRate <= 7) {
            index = 16;
        } else if (winRate > 7 && winRate <= 10) {
            index = 17;
        } else if (winRate > 10 && winRate <= 15) {
            index = 18;
        } else if (winRate > 15 && winRate <= 20) {
            index = 19;
        } else if (winRate > 20 && winRate <= 30) {
            index = 20;
        } else if (winRate > 30 && winRate <= 50) {
            index = 21;
        } else if (winRate > 50 && winRate <= 100) {
            index = 22;
        } else if (winRate > 100) {
            index = 23;
        }

        if (index >= 0) {
            if (!countWinRate.containsKey(index)) {
                countWinRate.put(index, 0);
            }
            countWinRate.put(index, countWinRate.get(index) + 1);
        }
    }
    
    public void merge(RtpResult rtpResult) {
        this.betId = rtpResult.betId;
        this.currency = rtpResult.currency;
        this.betAmount = rtpResult.betAmount;
        this.totalWin += rtpResult.totalWin;
        this.totalBet += rtpResult.totalBet;
        this.totalJackPotWin += rtpResult.totalJackPotWin;
        this.numberJackpotWin += rtpResult.numberJackpotWin;
        this.totalJackpotWinMini += rtpResult.totalJackpotWinMini;
        this.numberJackpotWinMini += rtpResult.numberJackpotWinMini;
        this.totalJackpotWinMinor += rtpResult.totalJackpotWinMinor;
        this.numberJackpotWinMinor += rtpResult.numberJackpotWinMinor;
        this.totalJackpotWinMajor += rtpResult.totalJackpotWinMajor;
        this.numberJackpotWinMajor += rtpResult.numberJackpotWinMajor;
        this.totalJackpotWinGrand += rtpResult.totalJackpotWinGrand;
        this.numberJackpotWinGrand += rtpResult.numberJackpotWinGrand;
        this.totalMiniWin += rtpResult.totalMiniWin;
        this.numberMiniWin += rtpResult.numberMiniWin;
        this.totalFreeSpinWin += rtpResult.totalFreeSpinWin;
        this.numberFreeSpinWin += rtpResult.numberFreeSpinWin;
        this.numberFreeSpinCount += rtpResult.numberFreeSpinCount;
        this.totalNormalWin += rtpResult.totalNormalWin;
        this.numberNormalWin += rtpResult.numberNormalWin;
        this.numberSpin += rtpResult.numberSpin;
        this.numberHasK += rtpResult.numberHasK;
        this.totalNormalBigWin += rtpResult.totalNormalBigWin;
        this.totalFreeBigWin += rtpResult.totalFreeBigWin;
        this.totalBonusForNormalBigWin += rtpResult.totalBonusForNormalBigWin;
        this.totalBonusForFreeBigWin += rtpResult.totalBonusForFreeBigWin;
        this.numberNormalBigWin += rtpResult.numberNormalBigWin;
        this.numberFreeBigWin += rtpResult.numberFreeBigWin;
        this.numberBonusForNormalBigWin += rtpResult.numberBonusForNormalBigWin;
        this.numberBonusForFreeBigWin += rtpResult.numberBonusForFreeBigWin;
        
        this.totalNormalMegaBigWin += rtpResult.totalNormalMegaBigWin;
        this.totalFreeMegaBigWin += rtpResult.totalFreeMegaBigWin;
        this.totalBonusForNormalMegaBigWin += rtpResult.totalBonusForNormalMegaBigWin;
        this.totalBonusForFreeMegaBigWin += rtpResult.totalBonusForFreeMegaBigWin;
        this.numberNormalMegaBigWin += rtpResult.numberNormalMegaBigWin;
        this.numberFreeMegaBigWin += rtpResult.numberFreeMegaBigWin;
        this.numberBonusForNormalMegaBigWin += rtpResult.numberBonusForNormalMegaBigWin;
        this.numberBonusForFreeMegaBigWin += rtpResult.numberBonusForFreeMegaBigWin;
        
        this.totalNormalFortuneBigWin += rtpResult.totalNormalFortuneBigWin;
        this.totalFreeFortuneBigWin += rtpResult.totalFreeFortuneBigWin;
        this.totalBonusForNormalFortuneBigWin += rtpResult.totalBonusForNormalFortuneBigWin;
        this.totalBonusForFreeFortuneBigWin += rtpResult.totalBonusForFreeFortuneBigWin;
        this.numberNormalFortuneBigWin += rtpResult.numberNormalFortuneBigWin;
        this.numberFreeFortuneBigWin += rtpResult.numberFreeFortuneBigWin;
        this.numberBonusForNormalFortuneBigWin += rtpResult.numberBonusForNormalFortuneBigWin;
        this.numberBonusForFreeFortuneBigWin += rtpResult.numberBonusForFreeFortuneBigWin;
        
        this.totalBaseNormalAmount += rtpResult.totalBaseNormalAmount;
        this.totalBaseFreeAmount += rtpResult.totalBaseFreeAmount;
        this.numberBaseNormalWin += rtpResult.numberBaseNormalWin;
        this.numberBaseFreeWin += rtpResult.numberBaseFreeWin;
        
        for( String key : rtpResult.payLinesForNormalSpin.keySet()) {
            this.putPaylineForNormal(key, rtpResult.payLinesForNormalSpin.get(key));
        }
        
        for( String key : rtpResult.payLinesForFreeSpin.keySet()) {
            this.putPaylineForFree(key, rtpResult.payLinesForFreeSpin.get(key));
        }

        for (Integer key : rtpResult.countWinRate.keySet()) {
            this.putCountWinRate(key, rtpResult.countWinRate.get(key));
        }
        
        this.countNormalToBonusSpin += rtpResult.countNormalToBonusSpin;
        this.countNormalToFreeSpin += rtpResult.countNormalToFreeSpin;
        this.countFreeToBonusSpin += rtpResult.countFreeToBonusSpin;
        this.countFreeToFreeSpin += rtpResult.countFreeToFreeSpin;
        this.countWinAmountGreaterBet += rtpResult.countWinAmountGreaterBet;
        
        this.totalWinPSFree += rtpResult.totalWinPSFree;
        this.totalWinPSBonus += rtpResult.totalWinPSBonus;
        this.countPSWinFree += rtpResult.countPSWinFree;
        this.countPSWinBonus += rtpResult.countPSWinBonus;
        this.countDrawTicket += rtpResult.countDrawTicket;

        this.totalBigWinFromGT0To1 += rtpResult.totalBigWinFromGT0To1;
        this.totalBigWinFromGT1To5 += rtpResult.totalBigWinFromGT1To5;
        this.totalBigWinFrom5To10 += rtpResult.totalBigWinFrom5To10;
        this.totalBigWinFrom10To15 += rtpResult.totalBigWinFrom10To15;
        this.totalBigWinFrom15To20 += rtpResult.totalBigWinFrom15To20;
        this.totalBigWinFrom20To25 += rtpResult.totalBigWinFrom20To25;
        this.totalBigWinFrom25To30 += rtpResult.totalBigWinFrom25To30;
        this.totalBigWinFrom30To35 += rtpResult.totalBigWinFrom30To35;
        this.totalBigWinFrom35To40 += rtpResult.totalBigWinFrom35To40;
        this.totalBigWinFrom40To45 += rtpResult.totalBigWinFrom40To45;
        this.totalBigWinFrom45To50 += rtpResult.totalBigWinFrom45To50;
        this.totalBigWinFrom50Upto += rtpResult.totalBigWinFrom50Upto;
        this.numberBigWinFromGT0To1 += rtpResult.numberBigWinFromGT0To1;
        this.numberBigWinFromGT1To5 += rtpResult.numberBigWinFromGT1To5;
        this.numberBigWinFrom5To10 += rtpResult.numberBigWinFrom5To10;
        this.numberBigWinFrom10To15 += rtpResult.numberBigWinFrom10To15;
        this.numberBigWinFrom15To20 += rtpResult.numberBigWinFrom15To20;
        this.numberBigWinFrom20To25 += rtpResult.numberBigWinFrom20To25;
        this.numberBigWinFrom25To30 += rtpResult.numberBigWinFrom25To30;
        this.numberBigWinFrom30To35 += rtpResult.numberBigWinFrom30To35;
        this.numberBigWinFrom35To40 += rtpResult.numberBigWinFrom35To40;
        this.numberBigWinFrom40To45 += rtpResult.numberBigWinFrom40To45;
        this.numberBigWinFrom45To50 += rtpResult.numberBigWinFrom45To50;
        this.numberBigWinFrom50Upto += rtpResult.numberBigWinFrom50Upto;

        this.totalBigWinFrom50To55 += rtpResult.totalBigWinFrom50To55;
        this.totalBigWinFrom55To60 += rtpResult.totalBigWinFrom55To60;
        this.totalBigWinFrom60To65 += rtpResult.totalBigWinFrom60To65;
        this.totalBigWinFrom65To70 += rtpResult.totalBigWinFrom65To70;
        this.totalBigWinFrom70To75 += rtpResult.totalBigWinFrom70To75;
        this.totalBigWinFrom75To80 += rtpResult.totalBigWinFrom75To80;
        this.totalBigWinFrom80To85 += rtpResult.totalBigWinFrom80To85;
        this.totalBigWinFrom85To90 += rtpResult.totalBigWinFrom85To90;
        this.totalBigWinFrom90To95 += rtpResult.totalBigWinFrom90To95;
        this.totalBigWinFrom95To100 += rtpResult.totalBigWinFrom95To100;

        this.numberBigWinFrom50To55 += rtpResult.numberBigWinFrom50To55;
        this.numberBigWinFrom55To60 += rtpResult.numberBigWinFrom55To60;
        this.numberBigWinFrom60To65 += rtpResult.numberBigWinFrom60To65;
        this.numberBigWinFrom65To70 += rtpResult.numberBigWinFrom65To70;
        this.numberBigWinFrom70To75 += rtpResult.numberBigWinFrom70To75;
        this.numberBigWinFrom75To80 += rtpResult.numberBigWinFrom75To80;
        this.numberBigWinFrom80To85 += rtpResult.numberBigWinFrom80To85;
        this.numberBigWinFrom85To90 += rtpResult.numberBigWinFrom85To90;
        this.numberBigWinFrom90To95 += rtpResult.numberBigWinFrom90To95;
        this.numberBigWinFrom95To100 += rtpResult.numberBigWinFrom95To100;
    }
    
    public RtpPlaySessionStore display(String uuid,
                                       int modeDisplay, boolean isLastRunTime, long numberSpin, String serviceId) {
        StringBuilder resultBuilder = new StringBuilder();
        NumberFormat formatter = new DecimalFormat("#,###,###,###,###.##");
        
        double freeWinRate = (this.totalWinPSFree/this.countPSWinFree)/this.betAmount;
        double bonusWinRate = (this.totalWinPSBonus/this.countPSWinBonus)/this.betAmount;
        double winrate = (float)this.getCountWinAmountGreaterBet()/this.getNumberSpin();
        
        resultBuilder.append("\n Total RUN: "+formatter.format(numberSpin)+ " sprin times.\n");
        resultBuilder.append("totalWin:"+formatter.format(totalWin)+" \n" );
        resultBuilder.append("totalBet: "+formatter.format(totalBet) +" \n");
        resultBuilder.append("totalJackPotWin: "+formatter.format(totalJackPotWin)+" \n");
        resultBuilder.append("totalMiniWin :"+  formatter.format(totalMiniWin)+" \n");
        resultBuilder.append("totalFreeSpin :"+  formatter.format(totalFreeSpinWin)+"  \n");
        resultBuilder.append("totalNormalWin:  "+formatter.format(totalNormalWin)+" \n" );
        resultBuilder.append("-------------------------------------------------- \n" );
        resultBuilder.append("totalWinPSFree: "+  formatter.format(getTotalWinPSFree())+"  \n");
        resultBuilder.append("countPSWinFree: "+  formatter.format(getCountPSWinFree())+"  \n");
        resultBuilder.append("totalWinPSBonus: "+  formatter.format(getTotalWinPSBonus())+"  \n");
        resultBuilder.append("countPSWinBonus: "+  formatter.format(getCountPSWinBonus())+"  \n");
        resultBuilder.append("free winrate: "+  formatter.format(freeWinRate)+"  \n");
        resultBuilder.append("bonus winrate: "+  formatter.format(bonusWinRate)+"  \n");
        resultBuilder.append("winrate: "+  formatter.format(winrate)+"  \n");
        resultBuilder.append("Count WinGreateBet: "+  formatter.format(getCountWinAmountGreaterBet())+"  \n");
        
        resultBuilder.append("-------------------------------------------------- \n" );
        if(modeDisplay > 0) {
            resultBuilder.append("countNormalToFreeSpin: "+formatter.format(countNormalToFreeSpin)+" \n" );
            resultBuilder.append("countNormalToBonusSpin: "+formatter.format(countNormalToBonusSpin)+" \n" );
            resultBuilder.append("countFreeToFreeSpin: "+formatter.format(countFreeToFreeSpin)+" \n" );
            resultBuilder.append("countFreeToBonusSpin: "+formatter.format(countFreeToBonusSpin)+" \n" );
            resultBuilder.append(" --------------------------------------------\n");
            resultBuilder.append("numberMiniWin: "+numberMiniWin+"\n");
            resultBuilder.append("numberFreeSpinWin: "+numberFreeSpinWin+"\n");
            resultBuilder.append("numberFreeSpinCount: "+numberFreeSpinCount+"\n");
            resultBuilder.append(" --------------------------------------------\n");
            resultBuilder.append("numberJackPotWin:  "+numberJackpotWin+"\n");
            resultBuilder.append("totalJackPotWinMini: "+formatter.format(totalJackpotWinMini)+" \n");
            resultBuilder.append("numberJackPotWinMini:  "+numberJackpotWinMini+"\n");
            resultBuilder.append("totalJackPotWinMinor: "+formatter.format(totalJackpotWinMinor)+" \n");
            resultBuilder.append("numberJackPotWinMinor:  "+numberJackpotWinMinor+"\n");
            resultBuilder.append("totalJackPotWinMajor: "+formatter.format(totalJackpotWinMajor)+" \n");
            resultBuilder.append("numberJackPotWinMajor:  "+numberJackpotWinMajor+"\n");
            resultBuilder.append("totalJackPotWinGrand: "+formatter.format(totalJackpotWinGrand)+" \n");
            resultBuilder.append("numberJackPotWinGrand:  "+numberJackpotWinGrand+"\n");
            resultBuilder.append(" --------------------------------------------\n");
            resultBuilder.append("totalNormalBigWin :"+  formatter.format(totalNormalBigWin)+" \n");
            resultBuilder.append("numberNormalBigWin: "+numberNormalBigWin+"\n");
            resultBuilder.append("totalFreeBigWin :"+  formatter.format(totalFreeBigWin)+" \n");
            resultBuilder.append("numberFreeBigWin: "+numberFreeBigWin+"\n");
            resultBuilder.append("totalBonusForNormalBigWin :"+  formatter.format(totalBonusForNormalBigWin)+" \n");
            resultBuilder.append("numberBonusForNormalBigWin: "+numberBonusForNormalBigWin+"\n");
            resultBuilder.append("totalBonusForFreeBigWin :"+  formatter.format(totalBonusForFreeBigWin)+" \n");
            resultBuilder.append("numberBonusForFreeBigWin: "+numberBonusForFreeBigWin+"\n");
            resultBuilder.append(" --------------------------------------------\n");
            resultBuilder.append("totalNormalMegaBigWin :"+  formatter.format(totalNormalMegaBigWin)+" \n");
            resultBuilder.append("numberNormalMegaBigWin: "+numberNormalMegaBigWin+"\n");
            resultBuilder.append("totalFreeMegaBigWin :"+  formatter.format(totalFreeMegaBigWin)+" \n");
            resultBuilder.append("numberFreeMegaBigWin: "+numberFreeMegaBigWin+"\n");
            resultBuilder.append("totalBonusForNormalMegaBigWin :"+  formatter.format(totalBonusForNormalMegaBigWin)+" \n");
            resultBuilder.append("numberBonusForNormalMegaBigWin: "+numberBonusForNormalMegaBigWin+"\n");
            resultBuilder.append("totalBonusForFreeMegaBigWin :"+  formatter.format(totalBonusForFreeMegaBigWin)+" \n");
            resultBuilder.append("numberBonusForFreeMegaBigWin: "+numberBonusForFreeMegaBigWin+"\n");
            resultBuilder.append(" --------------------------------------------\n");
            resultBuilder.append("totalNormalFortuneBigWin :"+  formatter.format(totalNormalFortuneBigWin)+" \n");
            resultBuilder.append("numberNormalFortuneBigWin: "+numberNormalFortuneBigWin+"\n");
            resultBuilder.append("totalFreeFortuneBigWin :"+  formatter.format(totalFreeFortuneBigWin)+" \n");
            resultBuilder.append("numberFreeFortuneBigWin: "+numberFreeFortuneBigWin+"\n");
            resultBuilder.append("totalBonusForNormalFortuneBigWin :"+  formatter.format(totalBonusForNormalFortuneBigWin)+" \n");
            resultBuilder.append("numberBonusForNormalFortuneBigWin: "+numberBonusForNormalFortuneBigWin+"\n");
            resultBuilder.append("totalBonusForFreeFortuneBigWin :"+  formatter.format(totalBonusForFreeFortuneBigWin)+" \n");
            resultBuilder.append("numberBonusForFreeFortuneBigWin: "+numberBonusForFreeFortuneBigWin+"\n");

            resultBuilder.append("numberNormalWin:  "+numberNormalWin+" \n" );
            resultBuilder.append("numberHasK:  "+numberHasK+" \n" );
        }
        double rtp = (totalWin/totalBet)*100.000;
        double rtpNormal = (totalNormalWin/totalBet)*100.000;
        double rtpFreeSpin = (totalFreeSpinWin/totalBet)*100.000;
        double rtpMiniGame = (totalMiniWin/totalBet)*100.000;
        double rtpJackPotWin = (totalJackPotWin/totalBet)*100.000;
        
//        rtp = rtp.multiply(100.000);
        resultBuilder.append("===RTPSTART=======\n" );
        resultBuilder.append("RTP:  "+rtp+" \n" );
        resultBuilder.append("RTP Normal:  "+rtpNormal+" \n" );
        resultBuilder.append("RTP Free Spin:  "+rtpFreeSpin+" \n" );
        resultBuilder.append("RTP Mini Game:  "+rtpMiniGame+" \n" );
        resultBuilder.append("RTP Total Jackpot:  "+rtpJackPotWin+" \n" );
        resultBuilder.append("===RTPEND=======\n" );
        if(modeDisplay > 0) {
            resultBuilder.append(" =============RTP JACK POT============= \n");
            double rtpMini = (totalJackpotWinMini/totalBet)*100.000;
            resultBuilder.append("RTP MINI="+ rtpMini+" \n");
            double rtpMinor = (totalJackpotWinMinor/totalBet)*100.000;
            resultBuilder.append("RTP MINOR="+ rtpMinor+" \n");
            double rtpMajor = (totalJackpotWinMajor/totalBet)*100.000;
            resultBuilder.append("RTP MAJOR="+ rtpMajor+" \n");
            double rtpGrand = (totalJackpotWinGrand/totalBet)*100.000;
            resultBuilder.append("RTP GRAND="+ rtpGrand+" \n");
        }

        DiceMachineConfigForNormal slotMachineConfigNormal = BeanUtils.getBean(DiceMachineConfigForNormal.class);
        DenominationLevel betDemonPerTotalCredit = toBetPerLineModel(slotMachineConfigNormal, this.betId, this.currency);
        JackpotService jackpotService = BeanUtils.getBean(JackpotService.class);
        Map<String, Money> initJackpotGroup = initJackPots(slotMachineConfigNormal, betDemonPerTotalCredit);
        
        double rtpTotalRemaining = 0;
        double rtpTotalRemainingGrand = 0;
        double rtpTotalRemainingMajor = 0;
        double rtpTotalRemainingMinor = 0;
        double rtpTotalRemainingMini = 0;
        if(isLastRunTime) {
            resultBuilder.append("=========== REMAINING JACKPOT VALUE: ======= \n" );
            Iterator<String> its  = initJackpotGroup.keySet().iterator();
            while(its.hasNext()) {
                String key = its.next();
//                if(key.contains("_JP_")) {
                    Money initJackpotValue = initJackpotGroup.get(key);
                    double totalBet = getTotalBet();
                    Money remainingAmount  = jackpotService.awardJackpot(key, key, initJackpotValue.value().doubleValue());
                    remainingAmount = remainingAmount.subtract(initJackpotValue);
                    double rtpRemaining = (remainingAmount.value().doubleValue()/totalBet)*100.000;
                    rtpTotalRemaining +=  rtpRemaining;
                    if(modeDisplay > 0) {
                        resultBuilder.append("REMAINING JACKPOT VALUE key=" + key + "  -- value=" + formatter.format(remainingAmount.value().doubleValue())
                        + " BET "+formatter.format(totalBet)+ " ----- RTP Remaining=" + formatter.format(rtpRemaining) +"\n");
                    }
                    if(key.contains("_MINI")) {
                        rtpTotalRemainingMini += rtpRemaining;
                        double totalInitForMini = initJackpotValue.value().doubleValue()*numberJackpotWinMini;
                        double totalProgressMini = totalJackpotWinMini - totalInitForMini;
                        double rtpTotalInitForMini = (totalInitForMini/totalBet)*100.000;
                        double rtpProgressMini = (totalProgressMini/totalBet)*100.000;
                        double rtpProgressWith = ((totalProgressMini+remainingAmount.value().doubleValue())/totalBet)*100.000;
                        if(modeDisplay > 0) {
                            resultBuilder.append("CHECK JACKPOT FOR key: "+key +" ---- totalInitForMini="+formatter.format(totalInitForMini) +" ---- totalProgressMini="+ formatter.format(totalProgressMini)+" ---- totalProgressMiniWithRemaining="+ formatter.format(totalProgressMini+remainingAmount.value().doubleValue())+" \n");
                            resultBuilder.append("CHECK JACKPOT FOR key: "+key +" --- RTP Total INIT MINI="+rtpTotalInitForMini+" ---- RTP PROGRESS MINI="+ rtpProgressMini +"  --- RTP PROGRESS MINI WITH REMAINING="+rtpProgressWith+ "\n");
                        }
                    } else if(key.contains("_MINOR")) {
                        rtpTotalRemainingMinor += rtpRemaining;
                        double totalInitFor = initJackpotValue.value().doubleValue()*numberJackpotWinMinor;
                        double totalProgress = totalJackpotWinMinor - totalInitFor;
                        double rtpTotalInitFor = (totalInitFor/totalBet)*100.000;
                        double rtpProgress = (totalProgress/totalBet)*100.000;
                        double rtpProgressWith = ((totalProgress+remainingAmount.value().doubleValue())/totalBet)*100.000;
                        if(modeDisplay > 0) {
                            resultBuilder.append("CHECK JACKPOT FOR key: "+key +" ---- totalInitFor Minor="+formatter.format(totalInitFor) +" ---- totalProgress Minor="+ formatter.format(totalProgress)+" ---- totalProgress Minor WithRemaining="+ formatter.format(totalProgress+remainingAmount.value().doubleValue())+" \n");
                            resultBuilder.append("CHECK JACKPOT FOR key: "+key +" --- RTP Total INIT MINOR="+rtpTotalInitFor+" ---- RTP PROGRESS MINOR="+ rtpProgress +"  --- RTP PROGRESS MINOR WITH REMAINING="+ rtpProgressWith+"\n");
                        }
                    } else if(key.contains("_MAJOR")) {
                        rtpTotalRemainingMajor += rtpRemaining;
                        double totalInitFor = initJackpotValue.value().doubleValue()*numberJackpotWinMajor;
                        double totalProgress = totalJackpotWinMajor - totalInitFor;
                        double rtpTotalInitFor = (totalInitFor/totalBet)*100.000;
                        double rtpProgress = (totalProgress/totalBet)*100.000;
                        double rtpProgressWith = ((totalProgress+remainingAmount.value().doubleValue())/totalBet)*100.000;
                        if(modeDisplay > 0) {
                            resultBuilder.append("CHECK JACKPOT FOR key: "+key +" ---- totalInitFor MAJOR="+formatter.format(totalInitFor) +" ---- totalProgress MAJOR="+ formatter.format(totalProgress)+" ---- totalProgress MAJOR WithRemaining="+ formatter.format(totalProgress+remainingAmount.value().doubleValue())+" \n");
                            resultBuilder.append("CHECK JACKPOT FOR key: "+key +" --- RTP Total INIT MAJOR="+rtpTotalInitFor+" ---- RTP PROGRESS MAJOR="+ rtpProgress +"  --- RTP PROGRESS MAJOR WITH REMAINING="+ rtpProgressWith+"\n");
                        }
                    } else if(key.contains("_GRAND")) {
                        rtpTotalRemainingGrand += rtpRemaining;
                        double totalInitFor = initJackpotValue.value().doubleValue()*numberJackpotWinGrand;
                        double totalProgress = totalJackpotWinGrand- totalInitFor;
                        double rtpTotalInitFor = (totalInitFor/totalBet)*100.000;
                        double rtpProgress = (totalProgress/totalBet)*100.000;
                        double rtpProgressWith = ((totalProgress+remainingAmount.value().doubleValue())/totalBet)*100.000;
                        if(modeDisplay > 0) {
                            resultBuilder.append("CHECK JACKPOT FOR key: "+key +" ---- totalInitFor GRAND="+formatter.format(totalInitFor) +" ---- totalProgress GRAND="+ formatter.format(totalProgress)+" ---- totalProgress GRAND WithRemaining="+ formatter.format(totalProgress+remainingAmount.value().doubleValue())+" \n");
                            resultBuilder.append("CHECK JACKPOT FOR key: "+key +" --- RTP Total INIT GRAND="+rtpTotalInitFor+" ---- RTP PROGRESS GRAND="+ rtpProgress +"  --- RTP PROGRESS GRAND WITH REMAINING="+ rtpProgressWith+"\n");
                        }
                    }
//                }
            }
        } else {
            resultBuilder.append("=========== REMAINING JACKPOT VALUE: ======= \n" );
            Iterator<String> its  = initJackpotGroup.keySet().iterator();
            while(its.hasNext()) {
                String key = its.next();
//                if(key.contains("_JP_")) {
                    double totalBet = getTotalBet();
                    Money initJackpotValue = initJackpotGroup.get(key);
                    Money remainingAmount  = jackpotService.getJackpot(key, key);
                    remainingAmount = remainingAmount.subtract(initJackpotValue);
                    double rtpRemaining = (remainingAmount.value().doubleValue()/totalBet)*100.000;
                    
                    rtpTotalRemaining +=  rtpRemaining;
                    if(modeDisplay > 0) {
                        resultBuilder.append("REMAINING JACKPOT VALUE key=" + key + "  -- value=" + formatter.format(remainingAmount.value().doubleValue())
                        + " BET "+formatter.format(totalBet)+ " ----- RTP Remaining=" + formatter.format(rtpRemaining) +"\n");
                    }
                    if(key.contains("_MINI")) {
                        rtpTotalRemainingMini += rtpRemaining;
                        double totalInitForMini = initJackpotValue.value().doubleValue()*numberJackpotWinMini;
                        double totalProgressMini = totalJackpotWinMini - totalInitForMini;
                        double rtpTotalInitForMini = (totalInitForMini/totalBet)*100.000;
                        double rtpProgressMini = (totalProgressMini/totalBet)*100.000;
                        double rtpProgressWith = ((totalProgressMini+remainingAmount.value().doubleValue())/totalBet)*100.000;
                        if(modeDisplay > 0) {
                            resultBuilder.append("CHECK JACKPOT FOR key: "+key +" ---- totalInitForMini="+formatter.format(totalInitForMini) +" ---- totalProgressMini="+ formatter.format(totalProgressMini)+" ---- totalProgressMiniWithRemaining="+ formatter.format(totalProgressMini+remainingAmount.value().doubleValue())+" \n");
                            resultBuilder.append("CHECK JACKPOT FOR key: "+key +" --- RTP Total INIT MINI="+rtpTotalInitForMini+" ---- RTP PROGRESS MINI="+ rtpProgressMini +"  --- RTP PROGRESS MINI WITH REMAINING="+rtpProgressWith+ "\n");
                        }
                    } else if(key.contains("_MINOR")) {
                        rtpTotalRemainingMinor += rtpRemaining;
                        double totalInitFor = initJackpotValue.value().doubleValue()*numberJackpotWinMinor;
                        double totalProgress = totalJackpotWinMinor - totalInitFor;
                        double rtpTotalInitFor = (totalInitFor/totalBet)*100.000;
                        double rtpProgress = (totalProgress/totalBet)*100.000;
                        double rtpProgressWith = ((totalProgress+remainingAmount.value().doubleValue())/totalBet)*100.000;
                        if(modeDisplay > 0) {
                            resultBuilder.append("CHECK JACKPOT FOR key: "+key +" ---- totalInitFor Minor="+formatter.format(totalInitFor) +" ---- totalProgress Minor="+ formatter.format(totalProgress)+" ---- totalProgress Minor WithRemaining="+ formatter.format(totalProgress+remainingAmount.value().doubleValue())+" \n");
                            resultBuilder.append("CHECK JACKPOT FOR key: "+key +" --- RTP Total INIT MINOR="+rtpTotalInitFor+" ---- RTP PROGRESS MINOR="+ rtpProgress +"  --- RTP PROGRESS MINOR WITH REMAINING="+ rtpProgressWith+"\n");
                        }
                    } else if(key.contains("_MAJOR")) {
                        rtpTotalRemainingMajor += rtpRemaining;
                        double totalInitFor = initJackpotValue.value().doubleValue()*numberJackpotWinMajor;
                        double totalProgress = totalJackpotWinMajor - totalInitFor;
                        double rtpTotalInitFor = (totalInitFor/totalBet)*100.000;
                        double rtpProgress = (totalProgress/totalBet)*100.000;
                        double rtpProgressWith = ((totalProgress+remainingAmount.value().doubleValue())/totalBet)*100.000;
                        if(modeDisplay > 0) {
                            resultBuilder.append("CHECK JACKPOT FOR key: "+key +" ---- totalInitFor MAJOR="+formatter.format(totalInitFor) +" ---- totalProgress MAJOR="+ formatter.format(totalProgress)+" ---- totalProgress MAJOR WithRemaining="+ formatter.format(totalProgress+remainingAmount.value().doubleValue())+" \n");
                            resultBuilder.append("CHECK JACKPOT FOR key: "+key +" --- RTP Total INIT MAJOR="+rtpTotalInitFor+" ---- RTP PROGRESS MAJOR="+ rtpProgress +"  --- RTP PROGRESS MAJOR WITH REMAINING="+ rtpProgressWith+"\n");
                        }
                    } else if(key.contains("_GRAND")) {
                        rtpTotalRemainingGrand += rtpRemaining;
                        double totalInitFor = initJackpotValue.value().doubleValue()*numberJackpotWinGrand;
                        double totalProgress = totalJackpotWinGrand- totalInitFor;
                        double rtpTotalInitFor = (totalInitFor/totalBet)*100.000;
                        double rtpProgress = (totalProgress/totalBet)*100.000;
                        double rtpProgressWith = ((totalProgress+remainingAmount.value().doubleValue())/totalBet)*100.000;
                        if(modeDisplay > 0) {
                            resultBuilder.append("CHECK JACKPOT FOR key: "+key +" ---- totalInitFor GRAND="+formatter.format(totalInitFor) +" ---- totalProgress GRAND="+ formatter.format(totalProgress)+" ---- totalProgress GRAND WithRemaining="+ formatter.format(totalProgress+remainingAmount.value().doubleValue())+" \n");
                            resultBuilder.append("CHECK JACKPOT FOR key: "+key +" --- RTP Total INIT GRAND="+rtpTotalInitFor+" ---- RTP PROGRESS GRAND="+ rtpProgress +"  --- RTP PROGRESS GRAND WITH REMAINING="+ rtpProgressWith+"\n");
                        }
                    }
//                }
            }
        }
        resultBuilder.append("===RTPREMAININGSTART=======\n" );
        resultBuilder.append("REMAINING TOTAL RTP Remaining="+ rtpTotalRemaining+" \n");
        resultBuilder.append("===RTPREMAININGEND=======\n" );
        
        if(modeDisplay > 0) {
            if(!CollectionUtils.isEmpty(payLinesForNormalSpin)) {
                resultBuilder.append("=========== payLinesForNormalSpin: ======= \n" );
                Set<String> keys = new TreeSet<>(payLinesForNormalSpin.keySet());
                for(String key : keys) {
                    RtpSymbolWin sym = payLinesForNormalSpin.get(key);
                    resultBuilder.append("Symbol Code= "+sym.getCode() +" ---- total times win ="+sym.getCount()+ " --- totalWinAmount="+formatter.format(sym.getPayWin().value().doubleValue())+" \n" );
                }
            }

            if(!CollectionUtils.isEmpty(payLinesForFreeSpin)) {
                resultBuilder.append("=========== payLinesForFreeSpin: ======= \n" );
                Set<String> keys = new TreeSet<>(payLinesForFreeSpin.keySet());
                for(String key : keys) {
                    RtpSymbolWin sym = payLinesForFreeSpin.get(key);
                    resultBuilder.append("Symbol Code= "+sym.getCode() +" ---- total times win ="+sym.getCount()+ " --- totalWinAmount="+formatter.format(sym.getPayWin().value().doubleValue())+" \n" );
                }
            }

            resultBuilder.append("====BIG WIN START=======\n" );
            resultBuilder.append("totalAmountBigWin 1 < win < 5: "+  formatter.format(getTotalBigWinFromGT1To5()) +"\n");
            resultBuilder.append("totalAmountBigWin 5  <= win < 10: "+  formatter.format(getTotalBigWinFrom5To10()) +"\n");
            resultBuilder.append("totalAmountBigWin 10 <= win < 15: "+  formatter.format(getTotalBigWinFrom10To15()) +"\n");
            resultBuilder.append("totalAmountBigWin 15 <= win < 20: "+  formatter.format(getTotalBigWinFrom15To20()) +"\n");
            resultBuilder.append("totalAmountBigWin 20 <= win < 25: "+  formatter.format(getTotalBigWinFrom20To25()) +"\n");
            resultBuilder.append("totalAmountBigWin 25 <= win < 30: "+  formatter.format(getTotalBigWinFrom25To30()) +"\n");
            resultBuilder.append("totalAmountBigWin 30 <= win < 35: "+  formatter.format(getTotalBigWinFrom30To35()) +"\n");
            resultBuilder.append("totalAmountBigWin 40 <= win < 45: "+  formatter.format(getTotalBigWinFrom35To40()) +"\n");
            resultBuilder.append("totalAmountBigWin 45 <= win < 50: "+  formatter.format(getTotalBigWinFrom40To45()) +"\n");
            resultBuilder.append("totalAmountBigWin 50 <= win: "+  formatter.format(getTotalBigWinFrom50Upto()) +"\n");
            resultBuilder.append("----\n");
            resultBuilder.append("numberBigWin 1 < win < 5: "+  formatter.format(getNumberBigWinFromGT1To5()) +"\n");
            resultBuilder.append("numberBigWin 5  <= win < 10: "+  formatter.format(getNumberBigWinFrom5To10()) +"\n");
            resultBuilder.append("numberBigWin 10 <= win < 15: "+  formatter.format(getNumberBigWinFrom10To15()) +"\n");
            resultBuilder.append("numberBigWin 15 <= win < 20: "+  formatter.format(getNumberBigWinFrom15To20()) +"\n");
            resultBuilder.append("numberBigWin 20 <= win < 25: "+  formatter.format(getNumberBigWinFrom20To25()) +"\n");
            resultBuilder.append("numberBigWin 25 <= win < 30: "+  formatter.format(getNumberBigWinFrom25To30()) +"\n");
            resultBuilder.append("numberBigWin 30 <= win < 35: "+  formatter.format(getNumberBigWinFrom30To35()) +"\n");
            resultBuilder.append("numberBigWin 40 <= win < 45: "+  formatter.format(getNumberBigWinFrom35To40()) +"\n");
            resultBuilder.append("numberBigWin 45 <= win < 50: "+  formatter.format(getNumberBigWinFrom40To45()) +"\n");
            resultBuilder.append("numberBigWin 50 <= win: "+  formatter.format(getNumberBigWinFrom50Upto()) +"\n");
            resultBuilder.append("====BIG WIN END====\n" );
        }
        
        

        Object extraData = getAllInOneExtraData();

        return new RtpPlaySessionStore(uuid, totalWin,
                totalBet, totalJackPotWin, totalJackpotWinGrand, totalJackpotWinMajor, totalJackpotWinMinor, totalJackpotWinMini, 
                rtpTotalRemaining, rtpTotalRemainingGrand, rtpTotalRemainingMajor, rtpTotalRemainingMinor, rtpTotalRemainingMini, 
                countNormalToFreeSpin, countNormalToBonusSpin, countFreeToFreeSpin, countFreeToBonusSpin,
                numberJackpotWinGrand, numberJackpotWinMajor, numberJackpotWinMinor, numberJackpotWinMini,
                totalMiniWin, totalNormalWin,totalFreeSpinWin, 
                numberSpin, numberFreeSpinCount, countWinAmountGreaterBet, freeWinRate, bonusWinRate, countDrawTicket, resultBuilder.toString(), serviceId,
                totalNormalBigWin,totalFreeBigWin, totalBonusForNormalBigWin, 
                totalBonusForFreeBigWin, numberNormalBigWin, numberFreeBigWin, numberBonusForNormalBigWin, numberBonusForFreeBigWin,
                totalBigWinFromGT1To5,
                totalBigWinFrom5To10,
                totalBigWinFrom10To15,
                totalBigWinFrom15To20,
                totalBigWinFrom20To25,
                totalBigWinFrom25To30,
                totalBigWinFrom30To35,
                totalBigWinFrom35To40,
                totalBigWinFrom40To45,
                totalBigWinFrom45To50,
                totalBigWinFrom50Upto,
                numberBigWinFromGT1To5,
                numberBigWinFrom5To10,
                numberBigWinFrom10To15,
                numberBigWinFrom15To20,
                numberBigWinFrom20To25,
                numberBigWinFrom25To30,
                numberBigWinFrom30To35,
                numberBigWinFrom35To40,
                numberBigWinFrom40To45,
                numberBigWinFrom45To50,
                numberBigWinFrom50Upto,
                extraData
        );
    }

    public Map<String, Object> buildAllInOneData(Map<String, ?> rtpAllInOneConfig, RtpPlaySessionStore store) {
        Map<String, Object> dataStorage = new HashMap<>();
        dataStorage.put("numberSpin", store.getNumberSpin());

        NumberFormat formatter = new DecimalFormat("###.#######");
        NumberFormat formatterRtp = new DecimalFormat("###.#######");

        double rtp = (store.getTotalWin() / store.getTotalBet()) * 100.000;
        double rtpNormal = (store.getTotalNormalWin() / store.getTotalBet()) * 100.000;
        double rtpFreeSpin = (store.getTotalFreeWin() / store.getTotalBet()) * 100.000;
        double rtpBonus = (store.getTotalMiniWin() / store.getTotalBet()) * 100.000;
        double rtpJackpot = (store.getTotalJackPotWin() / store.getTotalBet()) * 100.000;
        double rtpMini = (store.getTotalJackPotMiniWin() / store.getTotalBet()) * 100.000;
        double rtpMinor = (store.getTotalJackPotMinorWin() / store.getTotalBet()) * 100.000;
        double rtpMajor = (store.getTotalJackPotMajorWin() / store.getTotalBet()) * 100.000;
        double rtpGrand = (store.getTotalJackPotGrandWin() / store.getTotalBet()) * 100.000;

        double winRate = 1.0 * store.getCountWinGreaterBet() / store.getNumberSpin() * 100.000;

        double triggerBigWin1 = 0;
        double triggerBigWin2 = 0;
        double triggerBigWin3 = 0;
        if (rtpAllInOneConfig.get("bigWin") != null) {
            List<String> bigWinsConfig = (List<String>) rtpAllInOneConfig.get("bigWin");
            Assert.isTrue(bigWinsConfig.size() == 3, "bigWin config must be 3 item");

            RtpBigWinAllInOne extraData = (RtpBigWinAllInOne) store.getExtraData();
            triggerBigWin1 = getTriggerBigWin(bigWinsConfig.get(0), store, extraData, store.getNumberSpin());
            triggerBigWin2 = getTriggerBigWin(bigWinsConfig.get(1), store, extraData, store.getNumberSpin());
            triggerBigWin3 = getTriggerBigWin(bigWinsConfig.get(2), store, extraData, store.getNumberSpin());
        }

        double bonusTriggerFromNormal = 1.0 * store.getNumberCountNormalToBonus() / store.getNumberSpin() * 100.000;
        double freeTriggerFromNormal = 1.0 * store.getNumberCountNormalToFree() / store.getNumberSpin() * 100.000;
        double bonusTriggerFromFree = 1.0 * store.getNumberCountFreeToBonus() / store.getNumberSpin() * 100.000;
        double freeTriggerFromFree = 1.0 * store.getNumberCountFreeToFree() / store.getNumberSpin() * 100.000;

        double triggerMini = 1.0 * store.getNumberCountMini() / store.getNumberSpin() * 100.000;
        double miniSlashTotalBet = store.getNumberCountMini() == 0 ? 0 :
                rtpMini * store.getNumberSpin() / store.getNumberCountMini() / 100.00;
        double triggerMinor = 1.0 * store.getNumberCountMinor() / store.getNumberSpin() * 100.000;
        double minorSlashTotalBet = store.getNumberCountMinor() == 0 ? 0 :
                rtpMinor * store.getNumberSpin() / store.getNumberCountMinor() / 100.00;
        double triggerMajor = 1.0 * store.getNumberCountMajor() / store.getNumberSpin() * 100.000;
        double majorSlashTotalBet = (store.getNumberCountMajor() == 0 ? 0 :
                rtpMajor * store.getNumberSpin() / store.getNumberCountMajor()) / 100.00;
        double triggerGrand = 1.0 * store.getNumberCountGrand() / store.getNumberSpin() * 100.000;
        double grandSlashTotalBet = store.getNumberCountGrand() == 0 ? 0 :
                rtpGrand * store.getNumberSpin() / store.getNumberCountGrand() / 100.00;

        double finalWin = rtpNormal + rtpFreeSpin + rtpBonus +
                rtpMini + store.getTotalRTPMiniRemaining() +
                rtpMinor + store.getTotalRTPMinorRemaining() +
                rtpMajor + store.getTotalRTPMajorRemaining() +
                rtpGrand + store.getTotalRTPGrandRemaining();


        dataStorage.put("Win Rate", formatter.format(winRate) + "%");
        dataStorage.put("Free Win Rate", formatter.format(store.getFreeWinRate()));
        dataStorage.put("Bonus Win Rate", formatter.format(store.getBonusWinRate()));
        dataStorage.put("%Trigger Thang Lon", formatter.format(triggerBigWin1) + "%");
        dataStorage.put("%Trigger Thang Cuc Lon", formatter.format(triggerBigWin2) + "%");
        dataStorage.put("%Trigger Thang Sieu Lon", formatter.format(triggerBigWin3) + "%");
        dataStorage.put("Normal RTP", formatterRtp.format(rtpNormal) + "%");
        dataStorage.put("Free RTP", formatterRtp.format(rtpFreeSpin) + "%");
        dataStorage.put("Bonus RTP", formatterRtp.format(rtpBonus) + "%");

        dataStorage.put("Bonus Trigger from Normal", formatter.format(bonusTriggerFromNormal) + "%");
        dataStorage.put("Free Trigger from Normal", formatter.format(freeTriggerFromNormal) + "%");
        dataStorage.put("Bonus Trigger from Free", formatter.format(bonusTriggerFromFree) + "%");
        dataStorage.put("Free Trigger from Free", formatter.format(freeTriggerFromFree) + "%");

        dataStorage.put("Mini RTP", formatterRtp.format(rtpMini) + "%");
        dataStorage.put("So lan Thang Mini", formatter.format(store.getNumberCountMini()));
        dataStorage.put("Ty le trigger Mini", formatter.format(triggerMini) + "%");
        dataStorage.put("Remaining Mini rtp", formatterRtp.format(store.getTotalRTPMiniRemaining()) + "%");
        dataStorage.put("Win Mini / 1 total bet", formatterRtp.format(miniSlashTotalBet));

        dataStorage.put("Minor RTP", formatterRtp.format(rtpMinor) + "%");
        dataStorage.put("So lan Thang Minor", formatter.format(store.getNumberCountMinor()));
        dataStorage.put("Ty le trigger Minor", formatter.format(triggerMinor) + "%");
        dataStorage.put("Remaining Minor rtp", formatterRtp.format(store.getTotalRTPMinorRemaining()) + "%");
        dataStorage.put("Win Minor / 1 total bet", formatterRtp.format(minorSlashTotalBet));

        dataStorage.put("Major RTP", formatterRtp.format(rtpMajor) + "%");
        dataStorage.put("So lan Thang Major", formatter.format(store.getNumberCountMajor()));
        dataStorage.put("Ty le trigger Major", formatter.format(triggerMajor) + "%");
        dataStorage.put("Remaining Major rtp", formatterRtp.format(store.getTotalRTPMajorRemaining()) + "%");
        dataStorage.put("Win Major / 1 total bet", formatterRtp.format(majorSlashTotalBet));

        dataStorage.put("Grand RTP", formatterRtp.format(rtpGrand) + "%");
        dataStorage.put("So lan Thang Grand", formatter.format(store.getNumberCountGrand()));
        dataStorage.put("Ty le trigger Grand", formatter.format(triggerGrand) + "%");
        dataStorage.put("Remaining Grand rtp", formatterRtp.format(store.getTotalRTPMajorRemaining()) + "%");
        dataStorage.put("Win Grand / 1 total bet", formatterRtp.format(grandSlashTotalBet));

        dataStorage.put("Final Win", formatterRtp.format(finalWin) + "%");

        return dataStorage;
    }

    protected double getTriggerBigWin(String bigWinConfig, RtpPlaySessionStore store, RtpBigWinAllInOne extraData, long numberSpin) {
        String[] bigWinArray = bigWinConfig.split("-");
        String from = bigWinArray[0];
        String to = bigWinArray[1];
        String fieldName = String.format("numberBigWinFrom%sTo%s", from, to);

        Field field = ReflectionUtils.findField(RtpResult.class, fieldName);
        if (field != null) {
            field.setAccessible(true);
            long numberBigWin = (long) ReflectionUtils.getField(field, this);
            return 1.0 * numberBigWin / numberSpin * 100.000;
        } else {
            int bf = Integer.parseInt(from);
            int bt = Integer.parseInt(to);
            long numberBigWin2 = 0;
            for (int i = bf; i < bt; i += 5) {
                String fieldName2 = String.format("numberBigWinFrom%dTo%d", i, i + 5);
                Field field2 = ReflectionUtils.findField(RtpResult.class, fieldName2);
                if (field2 != null) {
                    field2.setAccessible(true);
                    numberBigWin2 += (long) ReflectionUtils.getField(field2, this);
                }
            }
            return 1.0 * numberBigWin2 / numberSpin * 100.000;
        }
    }
    
    @Override
    public String toString() {
        StringBuilder resultBuilder = new StringBuilder();
        NumberFormat formatter = new DecimalFormat("#,###,###,###,###.##");
        double rtp = (totalWin/totalBet)*100.000;
        double rtpNormal = (totalNormalWin/totalBet)*100.000;
        double rtpFreeSpin = (totalFreeSpinWin/totalBet)*100.000;
        double rtpMiniGame = (totalMiniWin/totalBet)*100.000;
        double rtpJackPotWin = (totalJackPotWin/totalBet)*100.000;
        
//        rtp = rtp.multiply(100.000);
        resultBuilder.append("===RTPSTART=======\n" );
        resultBuilder.append("RTP:  "+rtp+" \n" );
        resultBuilder.append("RTP Normal:  "+rtpNormal+" \n" );
        resultBuilder.append("RTP Free Spin:  "+rtpFreeSpin+" \n" );
        resultBuilder.append("RTP Mini Game:  "+rtpMiniGame+" \n" );
        resultBuilder.append("RTP Total Jackpot:  "+rtpJackPotWin+" \n" );
        resultBuilder.append("===RTPEND=======\n" );
        return resultBuilder.toString();
    }
    
    public DenominationLevel toBetPerLineModel(DiceMachineConfigForNormal slotMachineConfig, String idBet, String currency) {
        char[] arrayId = idBet.toCharArray();
        return slotMachineConfig.getDenominationLevelsForBet(currency).stream()
                .filter(bet -> bet.id().equals(String.valueOf(arrayId[0])+"0")).findFirst()
                .orElseThrow(null);
    }
    
    public Map<String, Money> initJackPots(DiceMachineConfigForNormal slotMachineConfigNormal, DenominationLevel denominationLevel) {
        Map<String, Money> jackpotGroup = new HashMap<>();

        for (InitJackpotChild jpItem : slotMachineConfigNormal.initJackpotList()) {
            DenominationLevel denom = initJackPotDemon(slotMachineConfigNormal, denominationLevel.jackpotID() + jpItem.code());
            if(denom != null)
                jackpotGroup.put(denominationLevel.jackpotID() + jpItem.code(), denom.initJackpot());
        }
        return jackpotGroup;
    }
    
    public DenominationLevel initJackPotDemon(DiceMachineConfigForNormal slotMachineConfigNormal, String key) {
        Map<String, Money> jackpotGroup = new HashMap<>();

        for (DenominationLevel denom : slotMachineConfigNormal.getDenominationLevels()) {
            if(key.equals(denom.jackpotID())) {
                return denom;
            }
        }
        return null;
    }

    public void reportWinLose(StringBuilder resultBuilder) {
        countWinRate.keySet().stream().sorted().mapToInt(i -> i).forEach(key -> {
            switch (key) {
                case 0:
                    resultBuilder.append("Range: 0 : " + countWinRate.get(key) + "\n");
                    break;
                case 1:
                    resultBuilder.append("Range: 0 < x <= 0.25 : " + countWinRate.get(key) + "\n");
                    break;
                case 2:
                    resultBuilder.append("Range: 0.25 < x <= 0.5 : " + countWinRate.get(key) + "\n");
                    break;
                case 3:
                    resultBuilder.append("Range: 0.5 < x <= 0.75 : " + countWinRate.get(key) + "\n");
                    break;
                case 4:
                    resultBuilder.append("Range: 0.75 < x <= 0.9 : " + countWinRate.get(key) + "\n");
                    break;
                case 5:
                    resultBuilder.append("Range: 0.9 < x < 1 : " + countWinRate.get(key) + "\n");
                    break;
                case 6:
                    resultBuilder.append("Range: 1 = x : " + countWinRate.get(key) + "\n");
                    break;
                case 7:
                    resultBuilder.append("Range: 1 < x <= 1.25 : " + countWinRate.get(key) + "\n");
                    break;
                case 8:
                    resultBuilder.append("Range: 1.25 < x <= 1.5 : " + countWinRate.get(key) + "\n");
                    break;
                case 9:
                    resultBuilder.append("Range: 1.5 < x <= 1.75 : " + countWinRate.get(key) + "\n");
                    break;
                case 10:
                    resultBuilder.append("Range: 1.75 < x < 2 : " + countWinRate.get(key) + "\n");
                    break;
                case 11:
                    resultBuilder.append("Range: 2 = x : " + countWinRate.get(key) + "\n");
                    break;
                case 12:
                    resultBuilder.append("Range: 2 < x < 3 : " + countWinRate.get(key) + "\n");
                    break;
                case 13:
                    resultBuilder.append("Range: 3 = x : " + countWinRate.get(key) + "\n");
                    break;
                case 14:
                    resultBuilder.append("Range: 3 < x < 5 : " + countWinRate.get(key) + "\n");
                    break;
                case 15:
                    resultBuilder.append("Range: 5 = x : " + countWinRate.get(key) + "\n");
                    break;
                case 16:
                    resultBuilder.append("Range: 5 < x <= 7 : " + countWinRate.get(key) + "\n");
                    break;
                case 17:
                    resultBuilder.append("Range: 7 < x <= 10 : " + countWinRate.get(key) + "\n");
                    break;
                case 18:
                    resultBuilder.append("Range: 10 < x <= 15 : " + countWinRate.get(key) + "\n");
                    break;
                case 19:
                    resultBuilder.append("Range: 15 < x <= 20 : " + countWinRate.get(key) + "\n");
                    break;
                case 20:
                    resultBuilder.append("Range: 20 < x <= 30 : " + countWinRate.get(key) + "\n");
                    break;
                case 21:
                    resultBuilder.append("Range: 30 < x <= 50 : " + countWinRate.get(key) + "\n");
                    break;
                case 22:
                    resultBuilder.append("Range: 50 < x <= 100 : " + countWinRate.get(key) + "\n");
                    break;
                case 23:
                    resultBuilder.append("Range: 100 < x : " + countWinRate.get(key) + "\n");
                    break;
                default:
                    break;
            }
        });
    }
    
    protected RtpBigWinAllInOne getAllInOneExtraData() {
        return new RtpBigWinAllInOne(
                totalBigWinFrom50To55,
                totalBigWinFrom55To60,
                totalBigWinFrom60To65,
                totalBigWinFrom65To70,
                totalBigWinFrom70To75,
                totalBigWinFrom75To80,
                totalBigWinFrom80To85,
                totalBigWinFrom85To90,
                totalBigWinFrom90To95,
                totalBigWinFrom95To100,

                numberBigWinFrom50To55,
                numberBigWinFrom55To60,
                numberBigWinFrom60To65,
                numberBigWinFrom65To70,
                numberBigWinFrom70To75,
                numberBigWinFrom75To80,
                numberBigWinFrom80To85,
                numberBigWinFrom85To90,
                numberBigWinFrom90To95,
                numberBigWinFrom95To100
        );
    }
}
