package com.io.begstd.slot.repository;

import com.io.begstd.slot.model.gamerule.RTPSlotConfig;

import java.util.List;

public interface RedisConfigRTPRepository {

    void save(String serviceId, List<RTPSlotConfig> rtpconfig);

    String getRtp(String serviceId);
    void saveRtp(String serviceId, String rtpValue);

    void removeRtp(String serviceId);

    boolean isExistedRtp(String serviceId);

}
