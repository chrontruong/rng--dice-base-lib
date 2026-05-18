package com.io.begstd.slot.rtp.task;

import com.io.begstd.slot.command.SpinCmd;
import com.io.begstd.slot.model.app.BasePlaySession;
import com.io.begstd.slot.model.app.JackpotInfo;
import com.io.begstd.slot.model.config.IMiniSlotConfig;
import com.io.begstd.slot.model.domain.Money;
import com.io.begstd.slot.rtp.config.RtpProperties;
import com.io.begstd.slot.rtp.model.RtpResult;
import com.io.begstd.slot.rtp.model.RtpSymbolWin;
import com.io.begstd.slot.rtp.service.RtpService;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BaseRtpScenario implements RtpScenario {

    @Autowired
    private RtpService rtpService;

    @Autowired
    RtpProperties rtpProperties;

    @Setter
    private SpinCmd spinCmd;

    @Autowired
    private IMiniSlotConfig slotMachineConfigForMini;
    
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
            if(basePlaySession.hasBonusGame()){
                rtpResult.increaseCountNormalToBonusSpin();
                List<Integer> listPos = new ArrayList<Integer>();
                while(basePlaySession.hasBonusGame()) {
                    int idom = getMiniOpenCell(listPos, slotMachineConfigForMini.bonusMatrixSize());
                    if(idom >= 0) {
                        listPos.add(idom);
                        uuid = UUID.randomUUID();
                        basePlaySession = rtpService.executeMiniGame(basePlaySession, userId, uuid.toString(), idom);

                        if (basePlaySession.latestSymbolBonusGameOpend().value() > 0) {
                            calculateMiniGame(basePlaySession, rtpResult, true);
                        }
                    }
                }
           }
           while(basePlaySession.hasFreeGame()) {
               rtpResult.increaseCountNormalToFreeSpin();
               uuid = UUID.randomUUID();
               basePlaySession = rtpService.executeFreeGame(basePlaySession, userId, uuid.toString());
               if (basePlaySession.latestWinFreeGameCount() > 0) {
                   rtpResult.increaseCountFreeToFreeSpin();
               }
               calculateFreeGame(basePlaySession, rtpResult);
               if(basePlaySession.hasBonusGame()){
                   rtpResult.increaseCountFreeToBonusSpin();
                   List<Integer> listPos = new ArrayList<Integer>(); 
                   while(basePlaySession.hasBonusGame()) {
                       
                       int idom = getMiniOpenCell(listPos, slotMachineConfigForMini.bonusMatrixSize());
                       if(idom >= 0) {
                           listPos.add(idom);
                           uuid = UUID.randomUUID();
                           basePlaySession = rtpService.executeMiniGame(basePlaySession, userId, uuid.toString(), idom);
                           if (basePlaySession.latestSymbolBonusGameOpend().value() > 0) {
                               calculateMiniGame(basePlaySession, rtpResult, true);
                           }
                       }
                   }
               }
           }
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
            calculatePaylineForNormal(basePlaySession, rtpResult);
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
    
    private void calculateMiniGame(BasePlaySession basePlaySession, RtpResult rtpResult, boolean isNormal) {
        Money money = basePlaySession.bonusGameWinAmount();
        if (basePlaySession.latestSymbolBonusGameOpend().value()>0) {
            rtpResult.increaseMiniWin();
            double miniWin = basePlaySession.betDenom().amount().multiply(basePlaySession.latestSymbolBonusGameOpend().value()).value().doubleValue();
            rtpResult.addTotalMiniWin(miniWin);
            
            Money bigWin = Money.of(rtpResult.getTotalBet()).multiply(10);
            if(bigWin.isLessThanOrEqual(money)) {
                if(isNormal) {
                    rtpResult.addTotalBonusForNormalBigWin(money.value().doubleValue());
                    rtpResult.increaseNumberBonusForNormalBigWin();
                } else {
                    rtpResult.addTotalBonusForFreeBigWin(money.value().doubleValue());
                    rtpResult.increaseNumberBonusForFreeBigWin();
                }
            }
            
            Money megaWin = Money.of(rtpResult.getTotalBet()).multiply(30);
            if(megaWin.isLessThanOrEqual(money)) {
                if(isNormal) {
                    rtpResult.addTotalBonusForNormalMegaBigWin(money.value().doubleValue());
                    rtpResult.increaseNumberBonusForNormalMegaBigWin();
                } else {
                    rtpResult.addTotalBonusForFreeMegaBigWin(money.value().doubleValue());
                    rtpResult.increaseNumberBonusForFreeMegaBigWin();
                }
            }
            
            Money fortuneWin = Money.of(rtpResult.getTotalBet()).multiply(50);
            if(fortuneWin.isLessThanOrEqual(money)) {
                if(isNormal) {
                    rtpResult.addTotalBonusForNormalFortuneBigWin(money.value().doubleValue());
                    rtpResult.increaseNumberBonusForNormalFortuneBigWin();
                } else {
                    rtpResult.addTotalBonusForFreeFortuneBigWin(money.value().doubleValue());
                    rtpResult.increaseNumberBonusForFreeFortuneBigWin();
                }
            }
        }
    }
//    private void calculateMiniGame(PlaySession playSession, RtpResult rtpResult, boolean isNormal) {
//        Money money = playSession.bonusGameWinAmount();
//        rtpResult.increaseNumberSpin();
//        if(money.gt(Money.ZERO)) {
//            rtpResult.increaseMiniWin();
//            rtpResult.addTotalMiniWin(playSession.bonusGameWinAmount().value().doubleValue());
//    //        Money money = playSession.bonusGameWinAmount();
//            
//            Money bigWin = Money.of(rtpResult.getTotalBet()).multiply(10);
//            if(bigWin.isLessThanOrEqual(money)) {
//                if(isNormal) {
//                    rtpResult.addTotalBonusForNormalBigWin(money.value().doubleValue());
//                    rtpResult.increaseNumberBonusForNormalBigWin();
//                } else {
//                    rtpResult.addTotalBonusForFreeBigWin(money.value().doubleValue());
//                    rtpResult.increaseNumberBonusForFreeBigWin();
//                }
//            }
//        }
//    }
    
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
    
    
    private void calculateFreeGame(BasePlaySession basePlaySession, RtpResult rtpResult) {
        rtpResult.increaseNumberSpin();
        rtpResult.increaseNumberFreeSpin();
        if(basePlaySession.latestWinAmount().gt(Money.ZERO)) {
            rtpResult.addTotalFreeSpinWin(basePlaySession.latestWinAmount().value().doubleValue());
        }

        if (basePlaySession.freeGamePayLines()!= null && basePlaySession.latestWinAmount() != null && basePlaySession.latestWinAmount().gt(Money.ZERO)) {
            Money money = basePlaySession.latestWinAmount();
            if (money.gt(Money.ZERO) ) { // win free game
                rtpResult.increaseFreeSpinWin();
                calculatePaylineForFree(basePlaySession, rtpResult);
                Money bigWin = Money.of(rtpResult.getBetAmount()).multiply(10);
                if(bigWin.isLessThanOrEqual(money)) {
                    rtpResult.addTotalFreeBigWin(money.value().doubleValue());
                    rtpResult.increaseNumberFreeBigWin();
                }
                
                Money megaWin = Money.of(rtpResult.getBetAmount()).multiply(30);
                if(megaWin.isLessThanOrEqual(money)) {
                    rtpResult.addTotalFreeMegaBigWin(money.value().doubleValue());
                    rtpResult.increaseNumberFreeMegaBigWin();
                }
                
                Money fortuneWin = Money.of(rtpResult.getBetAmount()).multiply(50);
                if(fortuneWin.isLessThanOrEqual(money)) {
                    rtpResult.addTotalFreeFortuneBigWin(money.value().doubleValue());
                    rtpResult.increaseNumberFreeFortuneBigWin();
                }
                
            }
         }
    }
    
    private void calculatePaylineForNormal(BasePlaySession basePlaySession, RtpResult rtpResult) {
        basePlaySession.normalGamePayLines().stream().forEach( (payLine) -> {
            RtpSymbolWin sym = parseSymbolWin(payLine);
            rtpResult.putPaylineForNormal(sym.getCode(), sym);
        });
    }

    private void calculatePaylineForFree(BasePlaySession basePlaySession, RtpResult rtpResult) {
        basePlaySession.freeGamePayLines().stream().forEach( (payLine) -> {
            RtpSymbolWin sym = parseSymbolWin(payLine);
            rtpResult.putPaylineForFree(sym.getCode(), sym);
        });
    }
    
    
    private int getMiniOpenCell(List<Integer> openedPosition, int lenght) {
        return -1;
    }

    private int getFreePos(List<Integer> freeSpinOption) {
        return -1;
    }    
        
    private RtpSymbolWin parseSymbolWin(String data) {
        String[] syms  = data.split(";");
        return new RtpSymbolWin(1l, Money.of(syms[2]), syms[3]);
    }
    
}
