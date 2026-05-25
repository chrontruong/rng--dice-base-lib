package com.io.begstd.dice.utils.kafka.impl;

import com.io.begstd.dice.utils.kafka.IExtraData;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
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
