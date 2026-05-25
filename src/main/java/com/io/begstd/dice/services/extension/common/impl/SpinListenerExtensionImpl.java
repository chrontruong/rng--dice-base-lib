package com.io.begstd.dice.services.extension.common.impl;

import com.io.begstd.dice.model.app.BasePlaySession;
import com.io.begstd.dice.model.config.ICommonDiceConfig;
import com.io.begstd.dice.model.config.DiceConfigMode;
import com.io.begstd.dice.services.extension.common.SpinListenerExtension;

import java.util.Map;

public class SpinListenerExtensionImpl implements SpinListenerExtension {

    @Override
    public BasePlaySession onAfterSpin(BasePlaySession basePlaySession, Map<DiceConfigMode, ICommonDiceConfig> configMapper) {
        return basePlaySession;
    }
}
