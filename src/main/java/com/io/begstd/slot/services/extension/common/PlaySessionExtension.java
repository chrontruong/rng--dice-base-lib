package com.io.begstd.slot.services.extension.common;

import com.io.begstd.extension.Extension;
import com.io.begstd.slot.command.SpinCmd;
import com.io.begstd.slot.model.app.BasePlaySession;
import com.io.begstd.slot.model.app.UserInfo;
import com.io.begstd.slot.model.config.ICommonSlotConfig;
import com.io.begstd.slot.model.config.SlotConfigMode;

import java.util.Map;

public interface PlaySessionExtension extends Extension {
    default String getName() {
        return this.getClass().getName();
    }
    BasePlaySession.BasePlaySessionBuilder<?, ?> createPlaySessionBuilderForNormal(String serviceId,
                                                                                   String uuid,
                                                                                   String commandId,
                                                                                   UserInfo userInfo,
                                                                                   SpinCmd cmd,
                                                                                   boolean isTrialMode);

    BasePlaySession.BasePlaySessionBuilder<?, ?> updatePlaySessionBuilderForFree(BasePlaySession basePlaySession, String commandId, UserInfo userInfo);

    BasePlaySession.BasePlaySessionBuilder<?, ?> updatePlaySessionBuilderForRespin(BasePlaySession basePlaySession, String commandId, UserInfo userInfo);

    BasePlaySession.BasePlaySessionBuilder<?, ?> updatePlaySessionBuilderForBonus(BasePlaySession basePlaySession, String commandId, UserInfo userInfo);

    BasePlaySession.BasePlaySessionBuilder<?, ?> updatePlaySessionBuilderForFreeOption(BasePlaySession basePlaySession, String commandId, UserInfo userInfo);

    BasePlaySession.BasePlaySessionBuilder<?, ?> updatePlaySessionBuilderForLightning(BasePlaySession basePlaySession, String commandId, UserInfo userInfo);

    BasePlaySession.BasePlaySessionBuilder<?, ?> updatePlaySessionBuilderForPowerUp(BasePlaySession basePlaySession, String commandId, UserInfo userInfo);
    
    BasePlaySession.BasePlaySessionBuilder<?, ?> updatePlaySessionBuilderForGamble(
            BasePlaySession basePlaySession, String commandId,
            double totalBet, int openCell, Map<SlotConfigMode, ICommonSlotConfig> configMapper, UserInfo userInfo);
    
}
