package com.io.begstd.slot.rtp.service;

import com.io.begstd.slot.command.SpinCmd;
import com.io.begstd.slot.grpc.promotionservice.PromotionData;
import com.io.begstd.slot.model.app.BasePlaySession;

public interface RtpService {
    BasePlaySession executeFreeGameOption(BasePlaySession basePlaySession, String userId, String commandId, int selectedOption);
    BasePlaySession executeFreeGame(BasePlaySession basePlaySession, String userId, String commandId);
    BasePlaySession executeMiniGame(BasePlaySession basePlaySession, String userId, String commandId, int openCell);
    BasePlaySession executeNormalGame(String userId, String commandId, SpinCmd cmd);

    int joinGame(String userId, String commandId, PromotionData promotion);
    int leaveGame(String userId, String commandId);
    
    BasePlaySession executePowerUpGame(BasePlaySession basePlaySession, String userId, String commandId, int openCell);
    BasePlaySession executeLightningGame(BasePlaySession basePlaySession, String userId, String commandId);
    BasePlaySession executeGambleGame(BasePlaySession basePlaySession, String userId, String commandId, int openCell, double totalBet);

    BasePlaySession executeRespinGame(BasePlaySession basePlaySession, String userId, String commandId);

    int joinGame(String userId, String commandId, PromotionData promotion, String currency);
    

}
