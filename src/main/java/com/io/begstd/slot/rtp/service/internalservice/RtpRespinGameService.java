package com.io.begstd.slot.rtp.service.internalservice;

import com.io.begstd.slot.model.app.BasePlaySession;

public interface RtpRespinGameService {
    BasePlaySession spin(BasePlaySession basePlaySession, String userId, String commandId);
}