package com.io.begstd.slot.services.extension.common;

import com.io.begstd.extension.Extension;
import com.io.begstd.slot.model.app.BasePlaySession;
import com.io.begstd.slot.model.config.ICommonSlotConfig;
import com.io.begstd.slot.model.config.SlotConfigMode;

import java.util.Map;

public interface SpinListenerExtension extends Extension {
    default String getName() {
        return this.getClass().getName();
    }
    BasePlaySession onAfterSpin(BasePlaySession basePlaySession, Map<SlotConfigMode, ICommonSlotConfig> configMapper);
}
