package com.io.begstd.dice.services.extension.common.impl;

import com.io.begstd.dice.model.app.BasePlaySession;
import com.io.begstd.dice.repository.RedisPlaySessionRepository;
import com.io.begstd.dice.services.extension.common.ResumeExtension;

public class ResumeExtensionImpl implements ResumeExtension {
    @Override
    public BasePlaySession getPlaySession(RedisPlaySessionRepository playSessionRepository, String serviceId, String userId, String currency) {
        return playSessionRepository.get(serviceId, userId, currency);
    }
}
