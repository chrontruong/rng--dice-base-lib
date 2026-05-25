package com.io.begstd.dice.model.app;

public enum PlayerStatus {
    ACTIVE(1),
    IDLE(2);

    private final int code;

    PlayerStatus(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
