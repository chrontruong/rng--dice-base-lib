package com.io.begstd.slot.model.config;

public enum SlotConfigMode {
    NORMAL,
    FREE,
    MINI,
    GAMBLE,
    LIGHTNING,
    POWERUP;

    public static SlotConfigMode getByName(String name) {
        for (SlotConfigMode mode : values()) {
            if (mode.name().equals(name)) {
                return mode;
            }
        }
        return null;
    }
}
