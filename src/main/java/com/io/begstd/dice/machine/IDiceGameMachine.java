package com.io.begstd.dice.machine;

import com.io.begstd.dice.model.app.BasePlaySession;
import com.io.begstd.dice.model.config.DiceConfigMode;
import com.io.begstd.dice.model.config.ICommonDiceConfig;

import java.util.Map;

public interface IDiceGameMachine {
    BasePlaySession spin(BasePlaySession currentSession, Map<DiceConfigMode, ICommonDiceConfig> configMapper);
}
