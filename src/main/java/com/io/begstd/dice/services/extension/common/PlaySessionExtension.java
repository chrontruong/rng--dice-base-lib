package com.io.begstd.dice.services.extension.common;

import com.io.begstd.extension.Extension;
import com.io.begstd.dice.model.app.BasePlaySession;
import com.io.begstd.dice.model.app.UserInfo;
import com.io.begstd.dice.command.SpinCmd;

public interface PlaySessionExtension extends Extension {
    default String getName() {
        return this.getClass().getName();
    }
    BasePlaySession.BasePlaySessionBuilder<?, ?> createPlaySessionBuilderForNormal(String serviceId,
                                                                                   String uuid,
                                                                                   String commandId,
                                                                                   UserInfo userInfo,
                                                                                   SpinCmd cmd,
                                                                                   boolean isTrialMode);
}
