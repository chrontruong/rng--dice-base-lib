package com.io.begstd.slot.services.extension.gameplay;

import com.io.begstd.extension.Extension;
import com.io.begstd.slot.model.app.BasePlaySession;

public interface GambleEndExtension extends Extension {
    default String getName() {
        return this.getClass().getName();
    }

    boolean isEndGamble(BasePlaySession basePlaySession, double totalBet);
}
