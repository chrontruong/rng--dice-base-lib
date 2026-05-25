package com.io.begstd.dice.services.internal;

import com.io.begstd.dice.model.app.UserInfo;

public interface BaseTrialService {
    void clearPlaySession(UserInfo userInfo);
    void clearProcessBeforeSpin(UserInfo userInfo);
    void clearProcessAfterSpin(UserInfo userInfo);
}
