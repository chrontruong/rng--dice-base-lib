package com.io.begstd.slot.rtp.service.internalservice;

import com.io.begstd.slot.common.SlotGameConstant;
import com.io.begstd.slot.model.app.BasePlaySession;
import com.io.begstd.slot.model.app.UserInfo;
import com.io.begstd.slot.model.config.ICommonSlotConfig;
import com.io.begstd.slot.model.config.SlotConfigMode;
import com.io.begstd.slot.services.internal.impl.PowerUpGameServiceImpl;
import com.io.begstd.slot.utils.ConfigManager;
import com.io.begstd.slot.utils.GameUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

public class BaseRtpPowerUpGameService implements RtpPowerUpGameService{

    @Autowired
    private PowerUpGameServiceImpl powerUpGameServiceImpl;

    @Autowired
    private ConfigManager configManager;
    
    public BasePlaySession spin(BasePlaySession basePlaySession, String userID, String commandId, int openCell) {
        Map<SlotConfigMode, ICommonSlotConfig> configMapper = configManager.getMainSlotConfigMap();
        UserInfo userInfo = UserInfo.builder()
                .userId(userID)
                .userType(SlotGameConstant.NBOT_TYPE)
                .userAgent("")
                .displayName(userID)
                .ip("ip")
                .currency(basePlaySession.currency())
                .build();
        BasePlaySession basePlaySessionAfterSpin = powerUpGameServiceImpl.execute(
                basePlaySession, commandId, userInfo, openCell, configMapper);
        return powerUpGameServiceImpl.finishPlaySessionRtp(basePlaySessionAfterSpin);
    }

}
