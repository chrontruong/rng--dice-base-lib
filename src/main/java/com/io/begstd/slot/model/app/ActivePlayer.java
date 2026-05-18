package com.io.begstd.slot.model.app;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Builder
@Getter
@Setter
@Accessors(fluent = true)
public class ActivePlayer {
    private String playerId;
    private PlayerStatus status;
}
