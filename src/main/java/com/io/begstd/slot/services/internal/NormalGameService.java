package com.io.begstd.slot.services.internal;

import com.io.begstd.slot.command.SpinCmd;
import com.io.begstd.slot.model.app.BasePlaySession;
import com.io.begstd.slot.model.app.UserInfo;

public interface NormalGameService {

    BasePlaySession spin(String commandId, UserInfo userInfo, SpinCmd cmd);
    BasePlaySession spinTrial(String commandId, UserInfo userInfo, SpinCmd cmd);
}
