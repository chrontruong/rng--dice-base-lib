package com.io.begstd.slot.rtp.service.internalservice;

import com.io.begstd.slot.command.SpinCmd;
import com.io.begstd.slot.model.app.BasePlaySession;

public interface RtpNormalGameService {
    BasePlaySession spin(String userId, String commandId, SpinCmd cmd);
}
