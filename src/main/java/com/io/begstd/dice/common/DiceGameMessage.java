package com.io.begstd.dice.common;

public enum DiceGameMessage {
   
    HAVE_PLAYSESSION("100001"),
    HAVE_COMMANDID("100009");

    private String messageCode;

    DiceGameMessage(String code) {
        this.messageCode = code;
    }

    public String getMessageCode() {
        return messageCode;
    }
}
