
package com.io.begstd.slot.model.config;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(fluent =  true)
public class TransformMatrixConfig {
    private String name;
    private int numberNewWild;
    private int[] reelChangeWild;
    
    // this field will be set by Transform service, not exist on config.
    private ISlotMachineConfig slotMachineConfig;
    
    //for football
    private List<Integer> winOverplay;
    private List<Integer> winOverplayRange;
    private List<Integer> wildnbr;
    private List<Integer> wildrand;
}
