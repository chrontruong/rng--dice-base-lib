package com.io.begstd.dice.rtp.service.internalservice;

import com.io.begstd.dice.model.app.BasePlaySession;
import com.io.begstd.dice.model.app.UserInfo;
import com.io.begstd.dice.model.config.ICommonDiceConfig;
import com.io.begstd.dice.model.config.DiceConfigMode;
import com.io.begstd.dice.services.internal.impl.NormalGameServiceImpl;
import com.io.begstd.dice.utils.ConfigManager;
import com.io.begstd.dice.command.SpinCmd;
import com.io.begstd.dice.common.DiceGameConstant;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

public class BaseRtpNormalGameService implements RtpNormalGameService {
    
    @Autowired
    private NormalGameServiceImpl normalGameServiceImpl;

    @Autowired
    private ConfigManager configManager;

    @Override
    public BasePlaySession spin(String userId, String commandId, SpinCmd cmd) {
        Map<DiceConfigMode, ICommonDiceConfig> configMapper = configManager.getMainDiceConfigMap();
        UserInfo userInfo = UserInfo.builder()
                .userId(userId)
                .userType(DiceGameConstant.NBOT_TYPE)
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
