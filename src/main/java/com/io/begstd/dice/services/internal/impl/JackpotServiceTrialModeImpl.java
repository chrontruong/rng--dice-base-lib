package com.io.begstd.dice.services.internal.impl;

import com.io.begstd.dice.model.domain.Money;
import com.io.begstd.dice.services.internal.JackpotTrialModeService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JackpotServiceTrialModeImpl implements JackpotTrialModeService {
    private Map<String, Money> jackpot = new HashMap<>();
    private String LOCK = "LOCK";

    @Override
    public boolean plusJackpot(String userId, String commandId, String jackpotId, double money) {
        synchronized (LOCK) {
            jackpot.computeIfPresent(jackpotId + userId, (key, jackpotAward)->{
                jackpotAward = jackpotAward.add(Money.of(money));
                return jackpotAward;
            });
        }
        return true;
    }

    @Override
    public Money awardJackpot(String userId, String commandId, String jackpotId, double money) {
        Money wonAmount = null;
        synchronized (LOCK) {
            wonAmount = Money.of(jackpot.get(jackpotId + userId).value());
            jackpot.put(jackpotId + userId, Money.of(money));
        }
        return wonAmount;
    }

    @Override
    public boolean initUserJackpot(String userId, Map<String, Money> JACKPOTS) {
        synchronized (LOCK) {
            JACKPOTS.forEach((k,v)->{
                jackpot.put(k+userId, v);
            });
        }
        return true;
    }

    @Override
    public boolean removeUserJackpot(String userId, Map<String, Money> JACKPOTS) {
        synchronized (LOCK) {
            JACKPOTS.forEach((k,v)->{
                jackpot.remove(k + userId);
            });
        }
        return true;
    }

    @Override
    public boolean plusMultipleJackpots(String userId, String commandId, Map<String, Money> JACKPOTS) {
        synchronized (LOCK) {
            JACKPOTS.forEach((k,v)->{
                jackpot.computeIfPresent(k + userId, (key, jackpotAward)->{
                    jackpotAward = jackpotAward.add(v);
                    return jackpotAward;
                });
            });
        }
        return true;
    }

    @Override
    public Map<String, Money> plusMultipleJackpotsWithAmount(String userId, String commandId, Map<String, Money> JACKPOTS) {
        Map<String, Money> result = new HashMap<String, Money>();
        synchronized (LOCK) {
            JACKPOTS.forEach((k,v)->{
                jackpot.computeIfPresent(k + userId, (key, jackpotAward)->{
                    jackpotAward = jackpotAward.add(v);
                    result.put(k + userId, jackpotAward);
                    return jackpotAward;
                });
            });
        }
        return result;
    }

    @Override
    public List<String> getUserJackpotList(String userId) {
        List<String> result = new ArrayList<String>();
        synchronized (LOCK) {
            jackpot.forEach((k,v)->{
                if(k.contains(userId)) {
                    String key = k.replace(userId, "");
                    String value = key+";"+v;
                    result.add(value);
                }
            });
        }
        return result;
    }
}