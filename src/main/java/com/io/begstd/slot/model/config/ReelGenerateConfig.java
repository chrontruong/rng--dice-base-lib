package com.io.begstd.slot.model.config;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(fluent = true)
public class ReelGenerateConfig {
    private int reelIdx;
    private int size;
    private String rule;
    private String rule1;
    private List<Integer> perConfig; // support for cluster
    private List<Integer> perConfigTrial; // support for cluster
}
