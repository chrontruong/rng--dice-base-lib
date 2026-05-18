package com.io.begstd.slot.repository;

import com.io.begstd.slot.model.config.SlotConfigMode;

public interface IRedisSlotConfigRepository {

    void saveMain(SlotConfigMode mode, String json, String clazz, String serviceId);

    boolean isExistedChannel(String channel);

    String get(String channel, String serviceId);

    void remove(String channel, String serviceId);

    void saveRtp(String serviceId, String rtp);
}
