package com.io.begstd.slot.services.extension.gameplay;

import com.io.begstd.extension.Extension;
import com.io.begstd.slot.model.app.BasePlaySession;
import com.io.begstd.slot.model.app.UserInfo;
import com.io.begstd.slot.model.config.ICommonSlotConfig;
import com.io.begstd.slot.model.config.SlotConfigMode;

import java.util.Map;

public interface GamblePlayExtension extends Extension{
    default String getName() {
        return this.getClass().getName();
    }
    
    BasePlaySession updateEndGamble(BasePlaySession basePlaySession, double totalBet, int openCell, String commandId,
                                    Map<SlotConfigMode, ICommonSlotConfig> configMapper, UserInfo userInfo);
    
}
