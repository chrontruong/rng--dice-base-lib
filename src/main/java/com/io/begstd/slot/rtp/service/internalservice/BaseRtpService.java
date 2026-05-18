package com.io.begstd.slot.rtp.service.internalservice;

import com.io.begstd.slot.command.SpinCmd;
import com.io.begstd.slot.grpc.promotionservice.PromotionData;
import com.io.begstd.slot.model.app.BasePlaySession;
import com.io.begstd.slot.model.app.UserInfo;
import com.io.begstd.slot.model.config.SlotMachineConfigForNormal;
import com.io.begstd.slot.rtp.service.RtpService;
import com.io.begstd.slot.services.internal.UserService;
import org.springframework.beans.factory.annotation.Autowired;

public class BaseRtpService implements RtpService {
    @Autowired
    RtpFreeGameService rtpFreeSpinGameService;
    
    @Autowired
    RtpRespinGameService rtpRespinGameService;
    
    @Autowired
    RtpNormalGameService rtpNormalSpinGameService;
    
    @Autowired
    RtpMiniGameService rtpMiniGameService;
    
    @Autowired
    RtpFreeSpinOptionGameService rtpFreeSpinOptionGameService;
    
    @Autowired
    UserService userService;
    @Autowired
    SlotMachineConfigForNormal slotMachineConfigForNormal;

    @Autowired
    RtpLightningGameService rtpLightningGameService;
    
    @Autowired
    RtpPowerUpGameService rtpPowerUpGameService;
    
    @Autowired
    RtpGambleGameService rtpGambleGameService;
    
    @Override
    public BasePlaySession executeFreeGameOption(BasePlaySession basePlaySession, String userId, String commandId, int selectedOption) {
        return rtpFreeSpinOptionGameService.spin(basePlaySession, userId, commandId, selectedOption);
    }

    @Override
    public BasePlaySession executeFreeGame(BasePlaySession basePlaySession, String userId, String commandId) {
        return rtpFreeSpinGameService.spin(basePlaySession, userId, commandId);
    }

    @Override
    public BasePlaySession executeRespinGame(BasePlaySession basePlaySession, String userId, String commandId) {
        return rtpRespinGameService.spin(basePlaySession, userId, commandId);
    }
    
    @Override
    public BasePlaySession executeMiniGame(BasePlaySession basePlaySession, String userId, String commandId, int openCell) {
        return rtpMiniGameService.play(basePlaySession, userId, commandId, openCell);
    }

    @Override
    public BasePlaySession executeNormalGame(String userId, String commandId, SpinCmd cmd) {
        return rtpNormalSpinGameService.spin(userId, commandId, cmd);
    }

    @Override
    public int joinGame(String userId, String commandId, PromotionData promotion) {
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
    public BasePlaySession executePowerUpGame(BasePlaySession basePlaySession, String userId, String commandId, int openCell) {
        return rtpPowerUpGameService.spin(basePlaySession, userId, commandId, openCell);
    }

    @Override
    public BasePlaySession executeLightningGame(BasePlaySession basePlaySession, String userId, String commandId) {
        return rtpLightningGameService.spin(basePlaySession, userId, commandId);
    }
    
    @Override
    public BasePlaySession executeGambleGame(BasePlaySession basePlaySession, String userId, String commandId, int openCell, double totalBet) {
        return rtpGambleGameService.play(basePlaySession, userId, commandId, openCell, totalBet);
    }

    @Override
    public int joinGame(String userId, String commandId, PromotionData promotion, String currency) {
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
