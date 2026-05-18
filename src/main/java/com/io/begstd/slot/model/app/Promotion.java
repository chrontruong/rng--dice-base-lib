package com.io.begstd.slot.model.app;

import lombok.Data;
import org.springframework.data.redis.core.RedisHash;

import java.io.Serializable;

@Data
@RedisHash("Promotion")
public class Promotion implements Serializable {
    /**
     *
     */
    private static final long serialVersionUID = -88683447497460662L;
    private String code;
    private String name;
    private boolean isValid;
    private long expireAt;
    private String betId;
    private int remain;
    private String userId;
    private int total;
    private int status;
    private String notifyStatus; //'new': user has added new promotion, 'reset': for daily reset.
    private String currency;
}
