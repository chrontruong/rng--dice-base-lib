package com.io.begstd.dice.repository;

import com.io.begstd.dice.model.gamerule.RTPDiceConfig;

import java.util.List;

public interface RedisConfigRTPRepository {

    void save(String serviceId, List<RTPDiceConfig> rtpconfig);

    String getRtp(String serviceId);
    void saveRtp(String serviceId, String rtpValue);

    void removeRtp(String serviceId);

    boolean isExistedRtp(String serviceId);

}
