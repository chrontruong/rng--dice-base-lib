package com.io.begstd.dice.repository;

import com.io.begstd.dice.model.config.DiceConfigMode;

public interface IRedisDiceConfigRepository {

    void saveMain(DiceConfigMode mode, String json, String clazz, String serviceId);

    boolean isExistedChannel(String channel);

    String get(String channel, String serviceId);

    void remove(String channel, String serviceId);

    void saveRtp(String serviceId, String rtp);
}
