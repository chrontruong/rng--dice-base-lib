package com.io.begstd.dice.repository.impl;

import com.io.begstd.dice.model.gamerule.RTPDiceConfig;
import com.io.begstd.dice.repository.RedisConfigRTPRepository;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class RedisConfigRTPRepositoryTestImpl implements RedisConfigRTPRepository {


    public RedisConfigRTPRepositoryTestImpl() {
    }

    @Override
    public synchronized void save(String serviceId, List<RTPDiceConfig> rtpconfig) {
        log.debug("Start Test save data {} rtp {}", serviceId, rtpconfig.toString());
    }

    @Override
    public synchronized String getRtp(String serviceId) {
        log.debug("Start Test save game {}", serviceId);
        return ""; // default 
    }

    @Override
    public synchronized void removeRtp(String serviceId) {
        log.debug("Start Test removeRtp {} serviceId {}", serviceId);
    }

    @Override
    public boolean isExistedRtp(String serviceId) {
        return true;
    }

    @Override
    public void saveRtp(String serviceId, String rtpValue) {
        log.debug("Start Test save data {} rtp {}", serviceId, rtpValue);

    }
}
