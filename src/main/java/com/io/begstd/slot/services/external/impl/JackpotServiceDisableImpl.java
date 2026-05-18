package com.io.begstd.slot.services.external.impl;

import com.io.begstd.slot.model.domain.Money;
import com.io.begstd.slot.services.external.JackpotService;

import java.util.Collections;
import java.util.Map;

public class JackpotServiceDisableImpl implements JackpotService {

    @Override
    public boolean plusJackpot(String commandID, String jackpotID, double money) {
        return false;
    }

    @Override
    public Money awardJackpot(String commandID, String jackpotID, double money) {
        return null;
    }

    @Override
    public Money getJackpot(String commandID, String jackpotID) {
        return null;
    }

    @Override
    public boolean initJackpot(String prefixService, String gameId, String commandID, Map<String, Money> JACKPOTS) {
        return true;
    }

    @Override
    public boolean plusMultipleJackpots(String commandID, Map<String, Money> JACKPOTS) {
        return true;
    }

    @Override
    public Map<String, Money> plusMultipleJackpotsWithAmount(String commandID, Map<String, Money> JACKPOTS) {
        return Collections.emptyMap();
    }
    
}
