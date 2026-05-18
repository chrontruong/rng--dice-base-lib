package com.io.begstd.slot.common;

public enum SlotGameMessage {
   
    HAVE_PLAYSESSION("100001"),
    HAVE_COMMANDID("100009");

    private String messageCode;

    SlotGameMessage(String code) {
        this.messageCode = code;
    }

    public String getMessageCode() {
        return messageCode;
    }
}
