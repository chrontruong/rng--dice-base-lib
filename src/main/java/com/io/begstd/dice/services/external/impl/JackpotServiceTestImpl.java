package com.io.begstd.dice.services.external.impl;

import com.io.begstd.dice.model.domain.Money;
import com.io.begstd.dice.services.external.JackpotService;

import java.util.HashMap;
import java.util.Map;

public class JackpotServiceTestImpl implements JackpotService {
    private Map<String, Money> jackpot;
    private Map<String, Integer> jackpotLevel;
    private String LOCK = "LOCK";

    @Override
    public boolean plusJackpot(String commandID, String jackpotID, double money) {
        synchronized (LOCK) {
            jackpot.computeIfPresent(jackpotID, (key, jackpotAward) -> {
                jackpotAward = jackpotAward.add(Money.of(money));
                return jackpotAward;
            });
        }
        return true;
    }

    @Override
    public Money awardJackpot(String commandID, String jackpotID, double money) {
        Money wonAmount = null;
        synchronized (LOCK) {
            wonAmount = Money.of(jackpot.get(jackpotID).value());
            jackpot.put(jackpotID, Money.of(money));
        }
        return wonAmount;
    }

    @Override
    public Money getJackpot(String commandID, String jackpotID) {
        Money wonAmount = null;
        synchronized (LOCK) {
            wonAmount = Money.of(jackpot.get(jackpotID).value());
        }
        return wonAmount;
    }

    @Override
    public boolean initJackpot(String prefixService, String gameId, String commandID, Map<String, Money> JACKPOTS) {
        jackpot = new HashMap<>();
        jackpotLevel = new HashMap<>();
        JACKPOTS.forEach((k, v) -> {
            jackpot.put(k, v);
        });
        return true;
    }

    @Override
    public boolean plusMultipleJackpots(String commandID, Map<String, Money> JACKPOTS) {
        synchronized (LOCK) {
            JACKPOTS.forEach((k, v) -> {
                jackpot.computeIfPresent(k, (key, jackpotAward) -> {
                    jackpotAward = jackpotAward.add(v);
                    return jackpotAward;
                });
            });
        }
        return true;
    }

    @Override
    public Map<String, Money> plusMultipleJackpotsWithAmount(String commandID, Map<String, Money> JACKPOTS) {
        Map<String, Money> result = new HashMap<String, Money>();
        synchronized (LOCK) {
            JACKPOTS.forEach((k, v) -> {
                jackpot.computeIfPresent(k, (key, jackpotAward) -> {
                    jackpotAward = jackpotAward.add(v);
                    result.put(k, jackpotAward);
                    return jackpotAward;
                });
            });
        }
        return result;
    }
    
}
