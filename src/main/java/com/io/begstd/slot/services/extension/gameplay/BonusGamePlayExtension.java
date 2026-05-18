package com.io.begstd.slot.services.extension.gameplay;

import com.io.begstd.extension.Extension;
import com.io.begstd.log.LogMessage;
import com.io.begstd.slot.model.app.BasePlaySession;
import com.io.begstd.slot.model.app.UserInfo;
import com.io.begstd.slot.model.config.ICommonSlotConfig;
import com.io.begstd.slot.model.config.SlotConfigMode;

import java.util.Map;

public interface BonusGamePlayExtension extends Extension {
    default String getName() {
        return this.getClass().getName();
    }
    BasePlaySession playBonus(BasePlaySession basePlaySession, UserInfo userInfo, String commandId, int openCell,
                              LogMessage.LogMessageBuilder logBuilder, Map<SlotConfigMode, ICommonSlotConfig> configMapper);
}
