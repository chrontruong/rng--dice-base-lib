package com.io.begstd.slot.model.config;

import com.io.begstd.slot.machine.wonrule.WonRuleExtension;
import com.io.begstd.slot.model.gamerule.RangeGamblerValue;
import com.io.begstd.slot.model.gamerule.SymbolGamble;

import java.util.List;

public interface ISlotConfigGamble extends ICommonSlotConfig{
    
    List<WonRuleExtension> wonRules();
    SymbolGamble getSymbolByCode(String code);
    SymbolGamble getSymbolById(int id);
    RangeGamblerValue getRangeGamblerById(int id);
    int numPlayTotalInGamble();
    double gamblerMultiply();
}
