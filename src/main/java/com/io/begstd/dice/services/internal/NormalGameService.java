package com.io.begstd.dice.services.internal;

import com.io.begstd.dice.model.app.BasePlaySession;
import com.io.begstd.dice.model.app.UserInfo;
import com.io.begstd.dice.command.SpinCmd;

public interface NormalGameService {

    BasePlaySession spin(String commandId, UserInfo userInfo, SpinCmd cmd);
    BasePlaySession spinTrial(String commandId, UserInfo userInfo, SpinCmd cmd);
}
