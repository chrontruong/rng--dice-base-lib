package com.io.begstd.slot.services.extension.common.impl;

import com.io.begstd.slot.model.app.BasePlaySession;
import com.io.begstd.slot.repository.RedisPlaySessionRepository;
import com.io.begstd.slot.services.extension.common.ResumeExtension;

public class ResumeExtensionImpl implements ResumeExtension {
    @Override
    public BasePlaySession getPlaySession(RedisPlaySessionRepository playSessionRepository, String serviceId, String userId, String currency) {
        return playSessionRepository.get(serviceId, userId, currency);
    }
}
