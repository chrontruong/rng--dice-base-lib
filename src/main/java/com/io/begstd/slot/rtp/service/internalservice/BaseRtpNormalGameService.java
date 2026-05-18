package com.io.begstd.slot.rtp.service.internalservice;

import com.io.begstd.slot.command.SpinCmd;
import com.io.begstd.slot.common.SlotGameConstant;
import com.io.begstd.slot.model.app.BasePlaySession;
import com.io.begstd.slot.model.app.UserInfo;
import com.io.begstd.slot.model.config.ICommonSlotConfig;
import com.io.begstd.slot.model.config.SlotConfigMode;
import com.io.begstd.slot.services.internal.impl.NormalGameServiceImpl;
import com.io.begstd.slot.utils.ConfigManager;
import com.io.begstd.slot.utils.GameUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

public class BaseRtpNormalGameService implements RtpNormalGameService{
    
    @Autowired
    private NormalGameServiceImpl normalGameServiceImpl;

    @Autowired
    private ConfigManager configManager;

    @Override
    public BasePlaySession spin(String userId, String commandId, SpinCmd cmd) {
        Map<SlotConfigMode, ICommonSlotConfig> configMapper = configManager.getMainSlotConfigMap();
        UserInfo userInfo = UserInfo.builder()
                .userId(userId)
                .userType(SlotGameConstant.NBOT_TYPE)
                .userAgent("")
                .displayName(userId)
                .ip("ip")
                .currency(cmd.currency())
                .build();
        BasePlaySession basePlaySessionAfterSpin = normalGameServiceImpl.execute(
                null, commandId, userInfo, cmd, false, configMapper);
        return normalGameServiceImpl.finishPlaySessionRtp(basePlaySessionAfterSpin);
    }

 }
