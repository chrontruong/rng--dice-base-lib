package com.io.begstd.slot.model.app;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.redis.core.RedisHash;

/**
 * User join game
 *
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@RedisHash("CommandIdHistory")

public class CommandIdHistory {
    /**
     * 
     */
    private String serviceId;
    private String commandId;
    private String userId;
    private boolean isSuccess;
    private String errorCode;
}
