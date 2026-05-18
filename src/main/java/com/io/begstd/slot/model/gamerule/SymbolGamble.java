package com.io.begstd.slot.model.gamerule;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.io.begstd.slot.model.playsession.ISymbol;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
@NoArgsConstructor(access = AccessLevel.PUBLIC)
public class SymbolGamble implements ISymbol{
    
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private int id;
    private String code;
    
    @Override
    public String name() {
        // TODO Auto-generated method stub
        return null;
    }
}
