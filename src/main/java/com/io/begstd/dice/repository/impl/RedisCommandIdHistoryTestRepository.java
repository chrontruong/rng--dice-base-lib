package com.io.begstd.dice.repository.impl;

import com.io.begstd.dice.model.app.CommandIdHistory;
import com.io.begstd.dice.repository.RedisCommandIdHistoryRepository;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RedisCommandIdHistoryTestRepository implements RedisCommandIdHistoryRepository {


    public RedisCommandIdHistoryTestRepository() {
    }

    @Override
    public void save(CommandIdHistory commandIdObj) {
        log.debug("Start Test save data {}", commandIdObj);
    }
    
    @Override
    public CommandIdHistory get(String commandId, String serviceId) {
        log.debug("Start Test Get data {}", commandId);

        return null;
    }

    @Override
    public boolean isExistedCommandId(String commandId, String serviceId) {
        return false;
    }

    @Override
    public void saveIfAbsent(CommandIdHistory commandId) {
        
        log.debug("Start Test saveIfAbsent {}", commandId);
       
    }
}
