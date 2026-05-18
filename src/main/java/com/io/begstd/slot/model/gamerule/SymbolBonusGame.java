package com.io.begstd.slot.model.gamerule;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.io.begstd.slot.model.playsession.ISymbolBonusGame;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@AllArgsConstructor(access = AccessLevel.PUBLIC)
public class SymbolBonusGame implements ISymbolBonusGame{

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    protected int id;

    protected String code;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    protected String name;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    protected float value;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private int max;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private int min;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private float multiplier;
}
