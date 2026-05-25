package com.io.begstd.dice.rtp.service;

import com.io.begstd.dice.model.app.BasePlaySession;
import com.io.begstd.dice.command.SpinCmd;
import com.io.begstd.dice.grpc.promotionservice.DicePromotionData;

public interface RtpService {
    BasePlaySession executeNormalGame(String userId, String commandId, SpinCmd cmd);

    int joinGame(String userId, String commandId, DicePromotionData promotion);
    int leaveGame(String userId, String commandId);

    int joinGame(String userId, String commandId, DicePromotionData promotion, String currency);
    

}
