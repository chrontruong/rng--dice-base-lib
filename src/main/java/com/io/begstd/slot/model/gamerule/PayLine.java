package com.io.begstd.slot.model.gamerule;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Getter
@Accessors(fluent = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PayLine implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -5499435328550237357L;

    private int id;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private int[][] verifyView;

}
