package com.io.begstd.slot.model.app;

import com.io.begstd.slot.model.wallet.WalletOption;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Builder
@Accessors(fluent = true)
public class UserInfo {
    private String userId;
    private String userType;
    private String userAgent;
    private String displayName;
    private double money;
    private String ip;
//    private String eventId;
    private WalletOption walletOption;
    private String avatar;
    private int env;
    private String ssid;
    private String currency;
    private String userBrand;
}
