package com.io.begstd.dice.services.internal;

import com.io.begstd.dice.model.domain.Money;

import java.util.List;
import java.util.Map;

public interface JackpotTrialModeService {
    boolean plusJackpot(String userId, String commandId, String jackpotId, double money);
    Money awardJackpot(String userId, String commandId, String jackpotId, double money);
    boolean initUserJackpot(String userId, Map<String, Money> JACKPOTS);
    boolean removeUserJackpot(String userId, Map<String, Money> JACKPOTS);
    boolean plusMultipleJackpots(String userId, String commandId, Map<String, Money> JACKPOTS);
    Map<String, Money> plusMultipleJackpotsWithAmount(String userId, String commandId, Map<String, Money> JACKPOTS);
    List<String> getUserJackpotList(String userId);
}
