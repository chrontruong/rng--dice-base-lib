package com.io.begstd.slot.utils.kafka.impl;

import com.io.begstd.slot.utils.kafka.IExtraData;
import lombok.*;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
@Builder (toBuilder = true )
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@AllArgsConstructor(access = AccessLevel.PUBLIC)
public class JackpotInfoKafka {
    private String jackpotId;
    private double jackpotAmount;
    private long winTime;
    private String userId;
    private String state;
    private int level;
    private IExtraData extraData;
}
