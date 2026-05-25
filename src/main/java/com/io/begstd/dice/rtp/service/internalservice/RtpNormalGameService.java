package com.io.begstd.dice.rtp.service.internalservice;

import com.io.begstd.dice.model.app.BasePlaySession;
import com.io.begstd.dice.command.SpinCmd;

public interface RtpNormalGameService {
    BasePlaySession spin(String userId, String commandId, SpinCmd cmd);
}
