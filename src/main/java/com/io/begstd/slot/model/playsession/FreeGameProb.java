package com.io.begstd.slot.model.playsession;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true)
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@AllArgsConstructor(access = AccessLevel.PUBLIC)
public class FreeGameProb implements IFreeGameProb{
    private int freeSpinWonId;
    private int wonCount;
    private float wonMultiplier;
}
