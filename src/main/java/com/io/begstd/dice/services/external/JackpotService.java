package com.io.begstd.dice.services.external;

import com.io.begstd.dice.model.domain.Money;

import java.util.Map;

public interface JackpotService {
    boolean plusJackpot(String commandID, String jackpotID, double money);
    Money awardJackpot(String commandID, String jackpotID, double money);
    boolean initJackpot(String prefixService, String gameId, String commandID, Map<String, Money> JACKPOTS);
    boolean plusMultipleJackpots(String commandID, Map<String, Money> JACKPOTS);
    Map<String, Money> plusMultipleJackpotsWithAmount(String commandID, Map<String, Money> JACKPOTS);
    Money getJackpot(String commandID, String jackpotID);
}
