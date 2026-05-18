package com.io.begstd.slot.model.gamerule;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

@Getter
@Accessors(fluent = true)
@NoArgsConstructor(access = AccessLevel.PUBLIC)
public class RangeGamblerValue implements Serializable {

    private static final long serialVersionUID = 7605890642786161985L;
    
    private int id;
    private List<Integer> value; // list of GambleSymbol id
    private List<Integer> range;
}
