package com.io.begstd.slot.model.app;

import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

@Getter
@Accessors(fluent = true)
@Builder(toBuilder = true)
@NoArgsConstructor()
@AllArgsConstructor()
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BigWinInfo {
    String winType;
    SpinState state;
    int level;
    @Setter
    double amount = 0;

    public enum SpinState {
        CURRENT,
        END
    }

    public BigWinInfo copy() {
        BigWinInfo bigWinInfo = new BigWinInfo();
        bigWinInfo.winType = this.winType;
        bigWinInfo.state = this.state;
        bigWinInfo.level = this.level;
        bigWinInfo.amount = this.amount;
        return bigWinInfo;
    }
}
