package com.io.begstd.slot.rtp.task;

import com.io.begstd.slot.model.domain.Money;
import com.io.begstd.slot.rtp.model.RtpResult;

import java.util.List;
import java.util.function.Function;

public interface RtpScenario {
    void setTotalBet(String totalBet, String currency, List<Integer> betLine);
    void setFreeSpinOption(int freeSpinOption);
    RtpResult run(String userId, long loopCount);

    default void calculateBigWin(Money checkAmount, Money betAmount, RtpResult rtpResult) {
        Function<Integer, Boolean> checkBigWin = (multiplier) -> {
            Money bigWin = betAmount.multiply(multiplier);
            
            if (checkAmount.isGreaterThan(Money.ZERO) 
                    && checkAmount.isLessThan(bigWin) 
                    && multiplier == 1) {
                rtpResult.addTotalBigWinFromGT0To1(checkAmount.value().doubleValue());
                rtpResult.increaseNumberBigWinFromGT0To1();
                
            } else if (checkAmount.isGreaterThanOrEqual(bigWin)) {
                if (multiplier >= 50) {
                    rtpResult.addTotalBigWinFrom50Upto(checkAmount.value().doubleValue());
                    rtpResult.increaseNumberBigWinFrom50Upto();
                }

                switch (multiplier) {
                    case 100:
                        return true;
                    case 95:
                        rtpResult.addTotalBigWinFrom95To100(checkAmount.value().doubleValue());
                        rtpResult.increaseNumberBigWinFrom95To100();
                        return true;
                    case 90:
                        rtpResult.addTotalBigWinFrom90To95(checkAmount.value().doubleValue());
                        rtpResult.increaseNumberBigWinFrom90To95();
                        return true;
                    case 85:
                        rtpResult.addTotalBigWinFrom85To90(checkAmount.value().doubleValue());
                        rtpResult.increaseNumberBigWinFrom85To90();
                        return true;
                    case 80:
                        rtpResult.addTotalBigWinFrom80To85(checkAmount.value().doubleValue());
                        rtpResult.increaseNumberBigWinFrom80To85();
                        return true;
                    case 75:
                        rtpResult.addTotalBigWinFrom75To80(checkAmount.value().doubleValue());
                        rtpResult.increaseNumberBigWinFrom75To80();
                        return true;
                    case 70:
                        rtpResult.addTotalBigWinFrom70To75(checkAmount.value().doubleValue());
                        rtpResult.increaseNumberBigWinFrom70To75();
                        return true;
                    case 65:
                        rtpResult.addTotalBigWinFrom65To70(checkAmount.value().doubleValue());
                        rtpResult.increaseNumberBigWinFrom65To70();
                        return true;
                    case 60:
                        rtpResult.addTotalBigWinFrom60To65(checkAmount.value().doubleValue());
                        rtpResult.increaseNumberBigWinFrom60To65();
                        return true;
                    case 55:
                        rtpResult.addTotalBigWinFrom55To60(checkAmount.value().doubleValue());
                        rtpResult.increaseNumberBigWinFrom55To60();
                        return true;
                    case 50:
                        rtpResult.addTotalBigWinFrom50To55(checkAmount.value().doubleValue());
                        rtpResult.increaseNumberBigWinFrom50To55();
                        return true;
                    case 45:
                        rtpResult.addTotalBigWinFrom45To50(checkAmount.value().doubleValue());
                        rtpResult.increaseNumberBigWinFrom45To50();
                        return true;
                    case 40:
                        rtpResult.addTotalBigWinFrom40To45(checkAmount.value().doubleValue());
                        rtpResult.increaseNumberBigWinFrom40To45();
                        return true;
                    case 35:
                        rtpResult.addTotalBigWinFrom35To40(checkAmount.value().doubleValue());
                        rtpResult.increaseNumberBigWinFrom35To40();
                        return true;
                    case 30:
                        rtpResult.addTotalBigWinFrom30To35(checkAmount.value().doubleValue());
                        rtpResult.increaseNumberBigWinFrom30To35();
                        return true;
                    case 25:
                        rtpResult.addTotalBigWinFrom25To30(checkAmount.value().doubleValue());
                        rtpResult.increaseNumberBigWinFrom25To30();
                        return true;
                    case 20:
                        rtpResult.addTotalBigWinFrom20To25(checkAmount.value().doubleValue());
                        rtpResult.increaseNumberBigWinFrom20To25();
                        return true;
                    case 15:
                        rtpResult.addTotalBigWinFrom15To20(checkAmount.value().doubleValue());
                        rtpResult.increaseNumberBigWinFrom15To20();
                        return true;
                    case 10:
                        rtpResult.addTotalBigWinFrom10To15(checkAmount.value().doubleValue());
                        rtpResult.increaseNumberBigWinFrom10To15();
                        return true;
                    case 5:
                        rtpResult.addTotalBigWinFrom5To10(checkAmount.value().doubleValue());
                        rtpResult.increaseNumberBigWinFrom5To10();
                        return true;
                    case 1:
                        if (checkAmount.isGreaterThan(bigWin)) {
                            rtpResult.addTotalBigWinFromGT1To5(checkAmount.value().doubleValue());
                            rtpResult.increaseNumberBigWinFromGT1To5();
                            return true;
                        }
                }
            }
            return false;
        };

        int[] bigWins = {
                1, 5, 10, 15, 20, 25, 30, 35, 40, 45, 50,
                55, 60, 65, 70, 75, 80, 85, 90, 95, 100
        };
        for (int i = bigWins.length - 1; i >= 0; i--) {
            if (checkBigWin.apply(bigWins[i])) {
                break;
            }
        }
    }
}
