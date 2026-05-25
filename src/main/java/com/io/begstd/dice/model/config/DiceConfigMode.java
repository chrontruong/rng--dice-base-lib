package com.io.begstd.dice.model.config;

public enum DiceConfigMode {
    NORMAL;

    public static DiceConfigMode getByName(String name) {
        for (DiceConfigMode mode : values()) {
            if (mode.name().equals(name)) {
                return mode;
            }
        }
        return null;
    }
}
