package com.io.begstd.slot.services.extension.common.impl;

import com.io.begstd.slot.model.app.BasePlaySession;
import com.io.begstd.slot.model.config.ICommonSlotConfig;
import com.io.begstd.slot.model.config.SlotConfigMode;
import com.io.begstd.slot.services.extension.common.SpinListenerExtension;

import java.util.Map;

public class SpinListenerExtensionImpl implements SpinListenerExtension {

    @Override
    public BasePlaySession onAfterSpin(BasePlaySession basePlaySession, Map<SlotConfigMode, ICommonSlotConfig> configMapper) {
        return basePlaySession;
    }
}
