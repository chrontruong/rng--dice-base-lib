package com.io.begstd.slot.repository;

import com.io.begstd.slot.model.app.CommandIdHistory;

public interface RedisCommandIdHistoryRepository {

    void save(CommandIdHistory commandIdObj);

    CommandIdHistory get(String commandId, String serviceId);

    boolean isExistedCommandId(String commandId, String serviceId);

    void saveIfAbsent(CommandIdHistory commandObj);
}
