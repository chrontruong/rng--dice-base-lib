package com.io.begstd.slot.rtp.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PUBLIC)
public class RtpPlaySessionStore {
    private String id;
    private double totalWin;
    private double totalBet;
    private double totalJackPotWin;
    private double totalJackPotGrandWin;
    private double totalJackPotMajorWin;
    private double totalJackPotMinorWin;
    private double totalJackPotMiniWin;

    private double totalRTPRemaining;
    private double totalRTPGrandRemaining;
    private double totalRTPMajorRemaining;
    private double totalRTPMinorRemaining;
    private double totalRTPMiniRemaining;
    private long numberCountNormalToFree;
    private long numberCountNormalToBonus;
    private long numberCountFreeToFree;
    private long numberCountFreeToBonus;
    private long numberCountGrand;
    private long numberCountMajor;
    private long numberCountMinor;
    private long numberCountMini;

    private double totalMiniWin;
    private double totalNormalWin;
    private double totalFreeWin;
    private long numberSpin;
    private long numberSpinFree;
    private long countWinGreaterBet;
    private double freeWinRate;
    private double bonusWinRate;
    private int countDrawTicket;
    private String message;
    private String serviceId;
    private double totalNormalBigWin;
    private double totalFreeBigWin;
    private double totalBonusForNormalBigWin;
    private double totalBonusForFreeBigWin;
    private long numberNormalBigWin;
    private long numberFreeBigWin;
    private long numberBonusForNormalBigWin;
    private long numberBonusForFreeBigWin;

    // bigwin (from inclusive to exclusive)
    private double totalBigWinFromGT1To5; // > 1 and < 5
    private double totalBigWinFrom5To10;
    private double totalBigWinFrom10To15;
    private double totalBigWinFrom15To20;
    private double totalBigWinFrom20To25;
    private double totalBigWinFrom25To30;
    private double totalBigWinFrom30To35;
    private double totalBigWinFrom35To40;
    private double totalBigWinFrom40To45;
    private double totalBigWinFrom45To50;
    private double totalBigWinFrom50Upto;
    private long numberBigWinFromGT1To5;
    private long numberBigWinFrom5To10;
    private long numberBigWinFrom10To15;
    private long numberBigWinFrom15To20;
    private long numberBigWinFrom20To25;
    private long numberBigWinFrom25To30;
    private long numberBigWinFrom30To35;
    private long numberBigWinFrom35To40;
    private long numberBigWinFrom40To45;
    private long numberBigWinFrom45To50;
    private long numberBigWinFrom50Upto;

    private Object extraData;

    public void setMessage(String message) {
        this.message = message;
    }

}
