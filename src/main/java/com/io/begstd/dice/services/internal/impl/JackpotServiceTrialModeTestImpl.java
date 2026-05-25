package com.io.begstd.dice.services.internal.impl;

import com.io.begstd.dice.model.domain.Money;
import com.io.begstd.dice.services.internal.JackpotTrialModeService;

import java.util.List;
import java.util.Map;

public class JackpotServiceTrialModeTestImpl implements JackpotTrialModeService {

    @Override
    public boolean plusJackpot(String userId, String commandId, String jackpotId, double money) {
        return true;
    }

    @Override
    public Money awardJackpot(String userId, String commandId, String jackpotId, double money) {
        return Money.of(money);
    }

    @Override
    public boolean initUserJackpot(String userId, Map<String, Money> JACKPOTS) {
        return true;
    }

    @Override
    public boolean removeUserJackpot(String userId, Map<String, Money> JACKPOTS) {
        return true;
    }

    @Override
    public boolean plusMultipleJackpots(String userId, String commandId, Map<String, Money> JACKPOTS) {
        return true;
    }

    @Override
    public Map<String, Money> plusMultipleJackpotsWithAmount(String userId, String commandId, Map<String, Money> JACKPOTS) {
        return null;
    }

    @Override
    public List<String> getUserJackpotList(String userId) {
        // TODO Auto-generated method stub
        return null;
    }
}
