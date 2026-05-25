package com.io.begstd.dice.services.internal.impl;

import com.io.begstd.dice.model.app.BasePlaySession;
import com.io.begstd.dice.model.domain.Money;
import com.io.begstd.dice.services.internal.WalletTrialModeService;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class WalletServiceTrialModeImpl implements WalletTrialModeService {
    private Map<String, Money> wallet = new HashMap<>();
    private String LOCK = "LOCK";
    @Override
    public Money getWalletAmount(String userId) {
        log.info("Get WalletTrialMode userName {}", userId);
        Money wonAmount = null;
        synchronized (LOCK) {
            if (wallet.get(userId) != null) {
                wonAmount = Money.of(wallet.get(userId).value());
            }
        }
        return wonAmount;
    }

    @Override
    public String initUserAmount(String userId, double amount) {
        log.info("Init WalletTrialMode userName {} -- amount={}", userId, Money.of(amount));
        synchronized (LOCK) {
            wallet.put(userId, Money.of(amount));
        }
        return "0";
    }
    
    @Override
    public String addWalletAmount(BasePlaySession basePlaySession, int totalCredit, String serviceName, String prefixService) {
        log.info("Add WalletTrialMode userName {} -- amount={}", basePlaySession.userId(), basePlaySession.winAmount());
        synchronized (LOCK) {
            wallet.computeIfPresent(basePlaySession.userId(), (key, addWallet)->{
                addWallet = addWallet.add(basePlaySession.winAmount());
                return addWallet;
            });
        }
        return "0";
    }

    @Override
    public String minusWalletAmount(BasePlaySession basePlaySession, double money, int totalCredit, String serviceName, String prefixService) {
        log.info("Minus WalletTrialMode userName {} -- amount={}", basePlaySession.userId(), money);
        try {
            Money result = getWalletAmount(basePlaySession.userId());
            if(result == null || result.lt(Money.of(money))) {
                return "2005";
            }
            synchronized (LOCK) {
                wallet.computeIfPresent(basePlaySession.userId(), (key, minusWallet)->{
                    minusWallet = minusWallet.subtract(Money.of(money));
                    return minusWallet;
                });
                
            }
            return "0";
        } catch (Exception e) {
            log.error("Minus WalletTrialMode userName {} -- Exception {}", basePlaySession.userId(), e);
            return "1";
        }
    }
    
    @Override
    public String removeUser(String userId) {
        log.info("removeUser WalletTrialMode userName {}", userId);
        synchronized (LOCK) {
            if(wallet != null) {
                wallet.remove(userId);
            }
        }
        return "0";
    }
    
    @Override
    public boolean isTheFirstTimeTrial(String userId) {
        synchronized (LOCK) {
            if(wallet != null && wallet.containsKey(userId)) {
                return false;
            }
        }
        return true;
    }
}
