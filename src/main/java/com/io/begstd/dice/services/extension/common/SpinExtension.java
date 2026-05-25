package com.io.begstd.dice.services.extension.common;

import com.io.begstd.extension.Extension;
import com.io.begstd.dice.machine.impl.BaseDiceMachineImpl;
import com.io.begstd.dice.model.app.BasePlaySession;
import com.io.begstd.dice.model.config.ICommonDiceConfig;
import com.io.begstd.dice.model.config.DiceConfigMode;

import java.util.Map;

public interface SpinExtension extends Extension {
    
    default String getName() {
        return this.getClass().getName();
    }
    
    BasePlaySession spin(BasePlaySession currentBasePlaySession,
                         Map<DiceConfigMode, ICommonDiceConfig> configMapper,
                         BaseDiceMachineImpl baseDiceMachine);
}
