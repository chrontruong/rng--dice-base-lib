package com.io.begstd.dice.model.config;

import com.io.begstd.log.LogMessage;
import com.io.begstd.log.LogsUtils;
import com.io.begstd.dice.factory.GameRuleFactory;

import java.lang.reflect.Field;

public interface ICommonDiceConfig {
    DiceConfigType getConfigType();

    void init();

    default void initInRuntime(GameRuleFactory gameRuleFactory) {
        try {
            Field field = this.getClass().getDeclaredField("gameRuleFactory");
            if (field != null) {
                field.setAccessible(true);
                if (field.get(this) == null && gameRuleFactory != null) {
                    field.set(this, gameRuleFactory);
                }
            }
        } catch (Exception e) {
            LogsUtils.writeLogException(LogMessage.builder().message(e.getMessage()).build(), e);
        }

        try {
            init();
        } catch (Exception e) {
            LogsUtils.writeLogException(LogMessage.builder().message(e.getMessage()).build(), e);
        }
    }
}
