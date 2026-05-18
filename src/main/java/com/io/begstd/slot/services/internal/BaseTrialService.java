package com.io.begstd.slot.services.internal;

import com.io.begstd.slot.model.app.UserInfo;

public interface BaseTrialService {
    void clearPlaySession(UserInfo userInfo);
    void clearProcessBeforeSpin(UserInfo userInfo);
    void clearProcessAfterSpin(UserInfo userInfo);
}
