package com.io.begstd.dice.model.config;

public enum DiceCurrencyType {
    ALL(99),
    USD(0);
    
    private int code;
    
    DiceCurrencyType(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

}
