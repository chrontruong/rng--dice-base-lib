package com.io.begstd.slot.services.extension.gameplay.impl;

import com.io.begstd.slot.model.app.BasePlaySession;
import com.io.begstd.slot.services.extension.gameplay.GambleEndExtension;

import java.time.Instant;

public class GambleEndExtensionImpl implements GambleEndExtension {

    @Override
    public boolean isEndGamble(BasePlaySession basePlaySession, double totalBet) {
        // is gamble expired?
        long timeStart = basePlaySession.savedTimeOfPlaySession();
        long expiredTime = basePlaySession.expiredTime();
        long currentTime = Instant.now().toEpochMilli();
        //end gamble: totalBet = 0 or current time > expired time - end
        return (totalBet == 0.0) || (currentTime - timeStart > expiredTime);
    }
}
