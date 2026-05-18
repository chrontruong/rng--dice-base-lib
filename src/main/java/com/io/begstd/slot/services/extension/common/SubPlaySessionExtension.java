package com.io.begstd.slot.services.extension.common;

import com.io.begstd.extension.Extension;
import com.io.begstd.slot.model.app.BasePlaySession;
import com.io.begstd.slot.model.config.ICommonSlotConfig;
import com.io.begstd.slot.model.config.SlotConfigMode;

import java.util.Map;

public interface SubPlaySessionExtension extends Extension{

    default String getName() {
        return this.getClass().getName();
    }
    
    default BasePlaySession setSubPlaySessionInfoNormal(BasePlaySession basePlaySession, Map<SlotConfigMode, ICommonSlotConfig> configMapper) {
        return basePlaySession;
    }
    
    default BasePlaySession setSubPlaySessionInfoFree(BasePlaySession basePlaySession, Map<SlotConfigMode, ICommonSlotConfig> configMapper) {
        return basePlaySession;
    }
    
    default BasePlaySession setSubPlaySessionInfoRespin(BasePlaySession basePlaySession, Map<SlotConfigMode, ICommonSlotConfig> configMapper) {
        return basePlaySession;
    }
    
    default BasePlaySession setSubPlaySessionInfoFreeOption(BasePlaySession basePlaySession, Map<SlotConfigMode, ICommonSlotConfig> configMapper) {
        return basePlaySession;
    }
    
    default BasePlaySession setSubPlaySessionInfoBonus(BasePlaySession basePlaySession, Map<SlotConfigMode, ICommonSlotConfig> configMapper) {
        return basePlaySession;
    }
    
    default BasePlaySession setSubPlaySessionInfoLightning(BasePlaySession basePlaySession, Map<SlotConfigMode, ICommonSlotConfig> configMapper) {
        return basePlaySession;
    }
    
    default BasePlaySession setSubPlaySessionInfoPowerUp(BasePlaySession basePlaySession, Map<SlotConfigMode, ICommonSlotConfig> configMapper) {
        return basePlaySession;
    }
    
    default BasePlaySession setSubPlaySessionInfoGamble(BasePlaySession basePlaySession, Map<SlotConfigMode, ICommonSlotConfig> configMapper) {
        return basePlaySession;
    }
}
