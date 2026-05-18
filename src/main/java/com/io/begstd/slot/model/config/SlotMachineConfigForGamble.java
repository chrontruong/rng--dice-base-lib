package com.io.begstd.slot.model.config;

import com.io.begstd.slot.factory.GameRuleFactory;
import com.io.begstd.slot.machine.wonrule.WonRuleExtension;
import com.io.begstd.slot.model.gamerule.RangeGamblerValue;
import com.io.begstd.slot.model.gamerule.SymbolGamble;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Data
@Accessors(fluent = true)
public final class SlotMachineConfigForGamble implements ISlotConfigGamble, InitializingBean {

    private String serviceId;
    private List<String> wonRulesName;
    
    @Autowired
    private GameRuleFactory gameRuleFactory;

    private List<WonRuleExtension> wonRules;
    
    private List<RangeGamblerValue> rangeGambler;

    private List<SymbolGamble> symbolGamble;
    private double gamblerMultiply;
    private int numPlayTotalInGamble;

    @Override
    public void afterPropertiesSet() throws Exception {
        init();
    }

    public void init() {
        initWinGameRules();
    }

    private void initWinGameRules() {
        wonRules = this.gameRuleFactory.getGameWonRules(this.wonRulesName);
    }

    public SymbolGamble getSymbolByCode(String code) {
        if (this.symbolGamble() == null) {
            return null;
        }
        return this.symbolGamble().stream().filter(e -> e.code().equals(code)).findFirst().orElse(null);
    }
    
    public SymbolGamble getSymbolById(int id) {
        if (this.symbolGamble() == null) {
            return null;
        }
        return this.symbolGamble().stream().filter(e -> e.id() == id).findFirst().orElse(null);
    }

    public RangeGamblerValue getRangeGamblerById(int id) {
        if (this.rangeGambler() == null) {
            return null;
        }
        return this.rangeGambler().stream().filter(e -> e.id() == id).findFirst().orElse(null);
    }

    @Override
    public SlotConfigType getConfigType() {
        return SlotConfigType.BASE_GAMBLE;
    }

}
