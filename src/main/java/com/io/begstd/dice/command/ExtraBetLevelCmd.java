package com.io.begstd.dice.command;

import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

@Data
@Accessors(fluent = true)
public class ExtraBetLevelCmd {
    private String id;

    private BigDecimal amount;
}
