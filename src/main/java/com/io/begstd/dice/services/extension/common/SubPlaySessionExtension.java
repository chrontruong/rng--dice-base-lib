package com.io.begstd.dice.services.extension.common;

import com.io.begstd.extension.Extension;
import com.io.begstd.dice.model.app.BasePlaySession;
import com.io.begstd.dice.model.config.ICommonDiceConfig;
import com.io.begstd.dice.model.config.DiceConfigMode;

import java.util.Map;

public interface SubPlaySessionExtension extends Extension{

    default String getName() {
        return this.getClass().getName();
    }
    
    default BasePlaySession setSubPlaySessionInfoNormal(BasePlaySession basePlaySession, Map<DiceConfigMode, ICommonDiceConfig> configMapper) {
        return basePlaySession;
    }
    
    default BasePlaySession setSubPlaySessionInfoFree(BasePlaySession basePlaySession, Map<DiceConfigMode, ICommonDiceConfig> configMapper) {
        return basePlaySession;
    }
    
    default BasePlaySession setSubPlaySessionInfoRespin(BasePlaySession basePlaySession, Map<DiceConfigMode, ICommonDiceConfig> configMapper) {
        return basePlaySession;
    }
    
    default BasePlaySession setSubPlaySessionInfoFreeOption(BasePlaySession basePlaySession, Map<DiceConfigMode, ICommonDiceConfig> configMapper) {
        return basePlaySession;
    }
    
    default BasePlaySession setSubPlaySessionInfoBonus(BasePlaySession basePlaySession, Map<DiceConfigMode, ICommonDiceConfig> configMapper) {
        return basePlaySession;
    }
    
    default BasePlaySession setSubPlaySessionInfoLightning(BasePlaySession basePlaySession, Map<DiceConfigMode, ICommonDiceConfig> configMapper) {
        return basePlaySession;
    }
    
    default BasePlaySession setSubPlaySessionInfoPowerUp(BasePlaySession basePlaySession, Map<DiceConfigMode, ICommonDiceConfig> configMapper) {
        return basePlaySession;
    }
    
    default BasePlaySession setSubPlaySessionInfoGamble(BasePlaySession basePlaySession, Map<DiceConfigMode, ICommonDiceConfig> configMapper) {
        return basePlaySession;
    }
}
