package com.io.begstd.slot.services.internal;

import com.io.begstd.slot.model.app.BasePlaySession;
import com.io.begstd.slot.model.app.UserInfo;

public interface GambleService {
    BasePlaySession spin(String commandId, UserInfo userInfo, int openCell, double totalBet);
    BasePlaySession spinTrial(String commandId, UserInfo userInfo, int openCell, double totalBet);
}
