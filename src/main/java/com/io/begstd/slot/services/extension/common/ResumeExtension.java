package com.io.begstd.slot.services.extension.common;

import com.io.begstd.extension.Extension;
import com.io.begstd.slot.model.app.BasePlaySession;
import com.io.begstd.slot.repository.RedisPlaySessionRepository;

public interface ResumeExtension extends Extension {
    default String getName() {
        return this.getClass().getName();
    }

    BasePlaySession getPlaySession(RedisPlaySessionRepository playSessionRepository, String serviceId, String userId, String currency);

}
