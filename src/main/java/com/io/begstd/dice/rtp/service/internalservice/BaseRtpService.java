package com.io.begstd.dice.rtp.service.internalservice;

import com.io.begstd.dice.model.app.BasePlaySession;
import com.io.begstd.dice.model.app.UserInfo;
import com.io.begstd.dice.model.config.DiceMachineConfig;
import com.io.begstd.dice.rtp.service.RtpService;
import com.io.begstd.dice.services.internal.UserService;
import com.io.begstd.dice.command.SpinCmd;
import com.io.begstd.dice.grpc.promotionservice.DicePromotionData;
import org.springframework.beans.factory.annotation.Autowired;

public class BaseRtpService implements RtpService {
    
    @Autowired
    RtpNormalGameService rtpNormalSpinGameService;
    
    @Autowired
    UserService userService;
    @Autowired
    DiceMachineConfig diceMachineConfigForNormal;

    @Override
    public BasePlaySession executeNormalGame(String userId, String commandId, SpinCmd cmd) {
        return rtpNormalSpinGameService.spin(userId, commandId, cmd);
    }

    @Override
    public int joinGame(String userId, String commandId, DicePromotionData promotion) {
        UserInfo userInfo = UserInfo.builder()
                .userId(userId)
                .displayName(userId)
                .money(100000)
                .userType("USER")
//                .eventId(eventId)
                .build();
        return userService.joinGame(commandId, userInfo, promotion, 1);
    }

    @Override
    public int leaveGame(String userId, String commandId) {
        return userService.leaveGame(commandId, UserInfo.builder().userId(userId).build());
    }

    @Override
    public int joinGame(String userId, String commandId, DicePromotionData promotion, String currency) {
        UserInfo userInfo = UserInfo.builder()
                .userId(userId)
                .displayName(userId)
                .money(100000)
                .userType("USER")
                .currency(currency)
                .build();
        return userService.joinGame(commandId, userInfo, promotion, 1);
    }
}
