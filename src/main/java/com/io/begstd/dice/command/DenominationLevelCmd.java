package com.io.begstd.dice.command;

import com.io.begstd.dice.common.DiceGameConstant;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.util.List;

@Data
@Accessors(fluent = true)
public class DenominationLevelCmd {

    private int indx;
    private String id;

    private BigDecimal amount;

    private String jackpotID;
    
    private List<Integer> env;
    
    private String curr = DiceGameConstant.CURRENCY_DEFAULT;
}
