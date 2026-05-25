package com.io.begstd.dice.model.app;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.data.redis.core.RedisHash;

/**
 * Group User Maintainance
 *
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(fluent = true)
@RedisHash("GroupUserMaintaince")

public class GroupUserMaintaince {
    /**
     * 
     */
    private String serviceId;
    private String prefixUser;
    private boolean isMaintain;
}
