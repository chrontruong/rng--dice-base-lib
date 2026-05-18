package com.io.begstd.slot.services.extension.common;

import com.io.begstd.extension.Extension;
import com.io.begstd.slot.model.app.BasePlaySession;
import com.io.begstd.slot.model.config.ISlotMachineConfig;
import com.io.begstd.slot.model.domain.Money;

public interface DenominationExtension extends Extension {
    default String getName() {
        return this.getClass().getName();
    }
    Money convertToTotalBet(BasePlaySession basePlaySession, ISlotMachineConfig slotMachineConfig);
}
