package com.io.begstd.dice.services.internal;

import com.io.begstd.dice.model.app.BasePlaySession;
import com.io.begstd.dice.model.domain.Money;

public interface WalletTrialModeService {
    
    public Money getWalletAmount(String userId);
    public String addWalletAmount(BasePlaySession basePlaySession, int totalCredit, String serviceName, String prefixService);
    public String minusWalletAmount(BasePlaySession basePlaySession, double money, int totalCredit, String serviceName, String prefixService);
//    public boolean initWallet();
    public String initUserAmount(String userId, double amount);
    public String removeUser(String userId);
    public boolean isTheFirstTimeTrial(String userId);
}
