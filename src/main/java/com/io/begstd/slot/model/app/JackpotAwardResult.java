package com.io.begstd.slot.model.app;

import com.io.begstd.slot.model.domain.Money;
import lombok.*;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
@Builder (toBuilder = true )
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@AllArgsConstructor(access = AccessLevel.PUBLIC)
public class JackpotAwardResult {
    private String jackpotId;
    private Money jackpotAmount;
    private int level;
}
