package com.io.begstd.slot.services.internal.impl;

import com.io.begstd.slot.model.app.BasePlaySession;
import com.io.begstd.slot.model.domain.Money;
import com.io.begstd.slot.services.internal.WalletTrialModeService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class WalletServiceTrialModeTestImpl implements WalletTrialModeService {

    @Override
    public Money getWalletAmount(String userId) {
        log.info("Get WalletTrialMode Test userName {}", userId);
        return Money.of(100000000);
    }
    
    @Override
    public String addWalletAmount(BasePlaySession basePlaySession, int totalCredit, String serviceName, String prefixService) {
        log.info("Add WalletTrialMode Test userName {} -- amount={}", basePlaySession.userId(), basePlaySession.winAmount());
        return "0";
    }

    @Override
    public String minusWalletAmount(BasePlaySession basePlaySession, double money, int totalCredit, String serviceName, String prefixService) {
        log.info("MinusWalletAmount Test userName {} -- amount={}", basePlaySession.userId(), basePlaySession.winAmount());
        return "0";
    }
    
//    @Override
//    public boolean initWallet() {
//        return true;
//    }
    
    @Override
    public String initUserAmount(String userId, double amount) {
        log.info("initUserAmount Trial Mode Test userName {} -- amount: {}", userId, amount);
        return "0";
    }
    
    @Override
    public String removeUser(String userId) {
        log.info("removeUser WalletTrialMode Test userName {}", userId);
        return "0";
    }
    
    @Override
    public boolean isTheFirstTimeTrial(String userId) {
        log.info("checking user is in WalletTrialMode Test userName {}", userId);
        return false;
    }
}
