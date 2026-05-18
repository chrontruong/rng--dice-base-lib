package com.io.begstd.slot.model.config;

public enum SlotCurrencyType {
    ALL(99),
    VND(0),
    USD(1);
    
    private int code;
    
    SlotCurrencyType(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

//    public static SlotCurrencyType parseCurrencyType(int code) {
//        for (SlotCurrencyType ct : values()) {
//            if (ct.getCode() == code) {
//                return ct;
//            }
//        }
//        // default is VND
//        return VND;
//    }
//    public static SlotCurrencyType getAvailableCurrency(String name) {
//        if (StringUtils.isEmpty(name)) {
//            return VND;
//        }
//        for (SlotCurrencyType ct : values()) {
//            if (ct.name().equals(name)) {
//                return ct;
//            }
//        }
//        // default is VND
//        return VND;
//    }
}
