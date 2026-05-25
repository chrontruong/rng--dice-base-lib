package com.io.begstd.dice.services.extension.common;

import com.io.begstd.extension.Extension;
import com.io.begstd.dice.model.app.BasePlaySession;
import com.io.begstd.dice.model.config.IDiceMachineConfig;
import com.io.begstd.dice.model.domain.Money;

public interface DenominationExtension extends Extension {
    default String getName() {
        return this.getClass().getName();
    }
    Money convertToTotalBet(BasePlaySession basePlaySession, IDiceMachineConfig slotMachineConfig);
}
