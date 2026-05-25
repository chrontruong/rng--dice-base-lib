package com.io.begstd.dice.model.app;

import com.io.begstd.dice.model.domain.Money;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
@Builder(toBuilder = true)
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@AllArgsConstructor(access = AccessLevel.PUBLIC)
public class JackpotInfo {
    private String jackpotId;
    private Money jackpotAmount;
    private long winTime;
    private String userId;
    private int state;
    private int level;
    private double initJPDefault;
    private String eventName;
}
