package com.io.begstd.dice.rtp.task;

import com.io.begstd.dice.model.app.BasePlaySession;
import com.io.begstd.dice.model.app.JackpotInfo;
import com.io.begstd.dice.model.domain.Money;
import com.io.begstd.dice.rtp.config.RtpProperties;
import com.io.begstd.dice.rtp.model.RtpResult;
import com.io.begstd.dice.rtp.service.RtpService;
import com.io.begstd.dice.command.SpinCmd;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.UUID;

public class BaseRtpScenario implements RtpScenario {

    @Autowired
    private RtpService rtpService;

    @Autowired
    RtpProperties rtpProperties;

    @Setter
    private SpinCmd spinCmd;
    
    @Override
    public void setTotalBet(String totalBet, String currency, List<Integer> betLines) {
        this.spinCmd = new SpinCmd().totalBetId(totalBet).lineIds(betLines).currency(currency);
    }

    @Override
    public void setFreeSpinOption(int freeSpinOption) {
        
    }

    public RtpResult run(String userId, long loopCount) {

        RtpResult rtpResult = new RtpResult();
        for (long i = 0; i < loopCount; i++) {
            UUID uuid = UUID.randomUUID();
            BasePlaySession basePlaySession = rtpService.executeNormalGame(userId, uuid.toString(), spinCmd);
            rtpResult.addTotalBet(basePlaySession.totalBet().value().doubleValue());
            rtpResult.setBetAmount(basePlaySession.totalBet().value().doubleValue());
            rtpResult.setBetId(this.spinCmd.totalBetId());
            calculateNormalSpin(basePlaySession, rtpResult);
            calculateJackpotGame(basePlaySession, rtpResult);
            calculateWinAmount(basePlaySession, rtpResult);
            calculateBigWin(basePlaySession.winAmount(), basePlaySession.totalBet(), rtpResult);
        }

        return rtpResult;
    }
    private void calculateWinAmount(BasePlaySession basePlaySession, RtpResult rtpResult) {
        rtpResult.addTotalWin(basePlaySession.winAmount().value().doubleValue());
        if (basePlaySession.winAmount().isGreaterThan(basePlaySession.totalBet())) {
            rtpResult.increaseCountWinAmountGreaterBet();
        }
    }
    
    private void calculateNormalSpin(BasePlaySession basePlaySession, RtpResult rtpResult) {
        if (basePlaySession.normalGameWinAmount().gt(Money.ZERO)) { // win normal
            rtpResult.increaseNormalWin();
            rtpResult.addTotalNormalWin(basePlaySession.normalGameWinAmount().value().doubleValue());
            Money money = basePlaySession.normalGameWinAmount();
            Money bigWin = Money.of(rtpResult.getBetAmount()).multiply(10);
            if(bigWin.isLessThanOrEqual(money)) {
                rtpResult.addTotalNormalBigWin(money.value().doubleValue());
                rtpResult.increaseNumberNormalBigWin();
            }
            
            Money megaWin = Money.of(rtpResult.getBetAmount()).multiply(30);
            if(megaWin.isLessThanOrEqual(money)) {
                rtpResult.addTotalNormalMegaBigWin(money.value().doubleValue());
                rtpResult.increaseNumberNormalMegaBigWin();
            }
            
            Money fortuneWin = Money.of(rtpResult.getBetAmount()).multiply(50);
            if(fortuneWin.isLessThanOrEqual(money)) {
                rtpResult.addTotalNormalFortuneBigWin(money.value().doubleValue());
                rtpResult.increaseNumberNormalFortuneBigWin();
            }
        }
        rtpResult.increaseNumberSpin();
    }
    
    private void calculateJackpotGame(BasePlaySession basePlaySession, RtpResult rtpResult) {
        if(basePlaySession.winJackpotAmount().gt(Money.ZERO)) {
            rtpResult.increaseJackpotWin();
            rtpResult.addTotalJackpotWin(basePlaySession.winJackpotAmount().value().doubleValue());
            for (JackpotInfo jackpotInfo : basePlaySession.jackpotHistory()) {
                if(jackpotInfo.jackpotId().contains("_MINI")) {
                    rtpResult.addTotalJackpotWinMini(basePlaySession.winJackpotAmount().value().doubleValue());
                    rtpResult.increaseNumberJackpotWinMini();
                } else if(jackpotInfo.jackpotId().contains("_MINOR")) {
                    rtpResult.addTotalJackpotWinMinor(basePlaySession.winJackpotAmount().value().doubleValue());
                    rtpResult.increaseNumberJackpotWinMinor();
                } else if(jackpotInfo.jackpotId().contains("_MAJOR")) {
                    rtpResult.addTotalJackpotWinMajor(basePlaySession.winJackpotAmount().value().doubleValue());
                    rtpResult.increaseNumberJackpotWinMajor();
                } else if(jackpotInfo.jackpotId().contains("_GRAND")) {
                    rtpResult.addTotalJackpotWinGrand(basePlaySession.winJackpotAmount().value().doubleValue());
                    rtpResult.increaseNumberJackpotWinGrand();
                }
            }
        }
    }
}
