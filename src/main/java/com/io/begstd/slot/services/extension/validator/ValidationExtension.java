package com.io.begstd.slot.services.extension.validator;

import com.io.begstd.extension.Extension;
import com.io.begstd.slot.model.app.BasePlaySession;
import com.io.begstd.slot.model.app.UserInfo;
import com.io.begstd.slot.model.config.IMiniSlotConfig;
import com.io.begstd.slot.model.config.ISlotConfigGamble;

public interface ValidationExtension extends Extension {
    default String getName() {
        return this.getClass().getName();
    }
    void validateInRespin(BasePlaySession basePlaySession, UserInfo userInfo, String commandId);

    void validateInFree(BasePlaySession basePlaySession, UserInfo userInfo, String commandId);
    void validateInBonus(BasePlaySession basePlaySession, UserInfo userInfo, String commandId, int openCell, IMiniSlotConfig configMini);
    void validateInFreeSpinOption(BasePlaySession basePlaySession, UserInfo userInfo, String commandId, int openCell);
    
    void validateInGamble(BasePlaySession basePlaySession, UserInfo userInfo, String commandId, double totalBet,
            int openCell, ISlotConfigGamble gambleConfig);
    
    void validateInLightning(BasePlaySession basePlaySession, UserInfo userInfo, String commandId);
    void validateInPowerUp(BasePlaySession basePlaySession, UserInfo userInfo, String commandId);
}
