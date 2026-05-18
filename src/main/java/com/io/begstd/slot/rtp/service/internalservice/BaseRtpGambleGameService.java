package com.io.begstd.slot.rtp.service.internalservice;

import com.io.begstd.slot.common.SlotGameConstant;
import com.io.begstd.slot.model.app.BasePlaySession;
import com.io.begstd.slot.model.app.UserInfo;
import com.io.begstd.slot.model.config.ICommonSlotConfig;
import com.io.begstd.slot.model.config.ISlotMachineConfig;
import com.io.begstd.slot.model.config.SlotConfigMode;
import com.io.begstd.slot.services.internal.impl.GambleServiceImpl;

import java.util.Map;

public class BaseRtpGambleGameService extends GambleServiceImpl implements RtpGambleGameService {

    @Override
    public BasePlaySession play(BasePlaySession basePlaySession, String userId, String commandId, int openCell, double totalBet) {
        ISlotMachineConfig slotMachineConfigNormal = (ISlotMachineConfig) configManager.getConfigMain(SlotConfigMode.NORMAL);
        UserInfo userInfo = UserInfo.builder()
                .userId(userId)
                .userType(SlotGameConstant.NBOT_TYPE)
                .userAgent("")
                .displayName(userId)
                .currency(basePlaySession.currency())
                .ip("ip")
                .build();
        BasePlaySession basePlaySessionAfterSpin = execute(basePlaySession, commandId,
                userInfo, openCell, totalBet, false, configManager.getMainSlotConfigMap());
        return finishPlaySession(basePlaySessionAfterSpin, slotMachineConfigNormal, userInfo);
    }

    @Override
    protected BasePlaySession finishPlaySession(BasePlaySession basePlaySession, ISlotMachineConfig slotMachineConfigNormal, UserInfo userInfo) {
        return finishPlaySessionRtp(basePlaySession);
    }
}
