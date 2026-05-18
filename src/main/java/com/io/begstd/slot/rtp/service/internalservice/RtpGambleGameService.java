package com.io.begstd.slot.rtp.service.internalservice;

import com.io.begstd.slot.model.app.BasePlaySession;

public interface RtpGambleGameService {
    BasePlaySession play(BasePlaySession basePlaySession, String userId, String commandId, int openCell, double totalBet);
}
