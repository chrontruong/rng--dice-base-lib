package com.io.begstd.slot.rtp.service.internalservice;

import com.io.begstd.slot.common.SlotGameConstant;
import com.io.begstd.slot.model.app.BasePlaySession;
import com.io.begstd.slot.model.app.UserInfo;
import com.io.begstd.slot.model.config.ICommonSlotConfig;
import com.io.begstd.slot.model.config.SlotConfigMode;
import com.io.begstd.slot.services.internal.impl.LightningGameServiceImpl;
import com.io.begstd.slot.utils.ConfigManager;
import com.io.begstd.slot.utils.GameUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

public class BaseRtpLightningGameService implements RtpLightningGameService{

    @Autowired
    private LightningGameServiceImpl lightningGameServiceImpl;

    @Autowired
    ConfigManager configManager;
    
    public BasePlaySession spin(BasePlaySession basePlaySession, String userID, String commandId) {
        Map<SlotConfigMode, ICommonSlotConfig> configMapper = configManager.getMainSlotConfigMap();
        UserInfo userInfo = UserInfo.builder()
                .userId(userID)
                .userType(SlotGameConstant.NBOT_TYPE)
                .userAgent("")
                .displayName(userID)
                .ip("ip")
                .currency(basePlaySession.currency())
                .build();
        BasePlaySession basePlaySessionAfterSpin = lightningGameServiceImpl.execute(
                basePlaySession, commandId, userInfo, configMapper);
        return lightningGameServiceImpl.finishPlaySessionRtp(basePlaySessionAfterSpin);
    }

}
