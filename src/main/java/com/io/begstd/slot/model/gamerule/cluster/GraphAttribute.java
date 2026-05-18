package com.io.begstd.slot.model.gamerule.cluster;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

@Getter
@Accessors(fluent = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GraphAttribute implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -5499435328550237357L;

    private int id;
    private List<Integer> vertex;

    private int[][] edgeView;

}
