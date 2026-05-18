package com.io.begstd.slot.machine;

import com.io.begstd.slot.model.app.BasePlaySession;
import com.io.begstd.slot.model.config.ICommonSlotConfig;
import com.io.begstd.slot.model.config.ISlotConfigGamble;
import com.io.begstd.slot.model.config.SlotConfigMode;

import java.util.Map;

public interface ISlotGameMachine {
    BasePlaySession spin(BasePlaySession currentSession, Map<SlotConfigMode, ICommonSlotConfig> configMapper);
    BasePlaySession playGamble(BasePlaySession currentSession, ISlotConfigGamble gambleConfig);
}
