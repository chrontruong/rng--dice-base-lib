package com.io.begstd.slot.services.internal;

import com.io.begstd.slot.model.app.BasePlaySession;
import com.io.begstd.slot.model.app.UserInfo;

public interface MiniGameService {
    BasePlaySession play(String commandId, UserInfo userInfo, int openCell);
    BasePlaySession playTrial(String commandId, UserInfo userInfo, int openCell);
}
