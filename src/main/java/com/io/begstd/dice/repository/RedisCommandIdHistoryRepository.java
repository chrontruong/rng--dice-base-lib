package com.io.begstd.dice.repository;

import com.io.begstd.dice.model.app.CommandIdHistory;

public interface RedisCommandIdHistoryRepository {

    void save(CommandIdHistory commandIdObj);

    CommandIdHistory get(String commandId, String serviceId);

    boolean isExistedCommandId(String commandId, String serviceId);

    void saveIfAbsent(CommandIdHistory commandObj);
}
