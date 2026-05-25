package com.io.begstd.dice.repository;

import com.io.begstd.dice.model.app.BasePlaySession;

public interface RedisPlaySessionRepository {

    void save(BasePlaySession basePlaySession);

    BasePlaySession get(String userID, String serviceId, String currency);

    void removePlaySession(BasePlaySession basePlaySession);

    void saveIfAbsent(BasePlaySession player);
    
    int deleteTrialPs(String serviceId, long timeDeleted);
}
