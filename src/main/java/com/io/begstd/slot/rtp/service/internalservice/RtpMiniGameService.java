package com.io.begstd.slot.rtp.service.internalservice;

import com.io.begstd.slot.model.app.BasePlaySession;

public interface RtpMiniGameService {
    BasePlaySession play(BasePlaySession basePlaySession, String userId, String commandId, int openCell);
}
