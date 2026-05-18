package com.io.begstd.slot.repository;

import com.io.begstd.slot.model.app.BasePlaySession;

public interface RedisPlaySessionRepository {

    void save(BasePlaySession basePlaySession);

    void saveGamble(BasePlaySession basePlaySession);

    BasePlaySession get(String userID, String serviceId, String currency);

    BasePlaySession getGamble(String userID, String serviceId, String currency);

    void removePlaySession(BasePlaySession basePlaySession);

    void saveIfAbsent(BasePlaySession player);

    void removeGamblePlaySession(BasePlaySession basePlaySession);
    
    int deleteTrialPs(String serviceId, long timeDeleted);
}
