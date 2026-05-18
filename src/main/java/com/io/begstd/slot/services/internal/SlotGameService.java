package com.io.begstd.slot.services.internal;

import com.io.begstd.slot.command.SpinCmd;
import com.io.begstd.slot.grpc.promotionservice.PromotionData;
import com.io.begstd.slot.model.app.BasePlaySession;
import com.io.begstd.slot.model.app.UserInfo;

public interface SlotGameService {
    int joinGame(String commandId, UserInfo userInfo, PromotionData promotion, int env);
    int leaveGame(String commandId, UserInfo userInfo);

    int leaveGameTrial(String commandId, UserInfo userInfo);

    BasePlaySession normalSpin(String commandId, UserInfo userInfo, SpinCmd cmd);
    BasePlaySession freeSpin(String commandId, UserInfo userInfo);
    BasePlaySession respin(String commandId, UserInfo userInfo);
    BasePlaySession freeSpinOption(String commandId, UserInfo userInfo, int selectedOption);
    BasePlaySession playMiniGame(String commandId, UserInfo userInfo, int openCell);
    BasePlaySession gamble(String commandId, UserInfo userInfo, int openCell, double totalBet);
    BasePlaySession powerUpSpin(String commandId, UserInfo userInfo, int openCell);
    BasePlaySession lightingSpin(String commandId, UserInfo userInfo);
    int getLatestState(String commandId, UserInfo userInfo);
    
    // int joinGameTrial(String userId, String commandId, String serviceId);
    BasePlaySession normalSpinTrial(String commandId, UserInfo userInfo, SpinCmd cmd);
    BasePlaySession freeSpinTrial(String commandId, UserInfo userInfo);
    BasePlaySession respinTrial(String commandId, UserInfo userInfo);
    BasePlaySession freeSpinOptionTrial(String commandId, UserInfo userInfo, int selectedOption);
    BasePlaySession playMiniGameTrial(String commandId, UserInfo userInfo, int openCell);
    BasePlaySession gambleTrial(String commandId, UserInfo userInfo, int openCell, double totalBet);
    BasePlaySession powerUpSpinTrial(String commandId, UserInfo userInfo, int openCell);
    BasePlaySession lightingSpinTrial(String commandId, UserInfo userInfo);

}
