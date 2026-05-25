package com.io.begstd.dice.services.internal;

import com.io.begstd.dice.model.app.BasePlaySession;
import com.io.begstd.dice.model.app.UserInfo;
import com.io.begstd.dice.command.SpinCmd;
import com.io.begstd.dice.grpc.promotionservice.DicePromotionData;

public interface DiceGameService {
    int joinGame(String commandId, UserInfo userInfo, DicePromotionData promotion, int env);
    int leaveGame(String commandId, UserInfo userInfo);

    int leaveGameTrial(String commandId, UserInfo userInfo);

    BasePlaySession normalSpin(String commandId, UserInfo userInfo, SpinCmd cmd);
    int getLatestState(String commandId, UserInfo userInfo);

    BasePlaySession normalSpinTrial(String commandId, UserInfo userInfo, SpinCmd cmd);

}
