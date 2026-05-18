package com.io.begstd.slot.model.app;

import com.io.begstd.slot.model.domain.Money;
import lombok.*;
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
