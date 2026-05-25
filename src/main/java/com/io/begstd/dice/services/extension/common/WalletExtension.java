package com.io.begstd.dice.services.extension.common;

import com.io.begstd.extension.Extension;
import com.io.begstd.dice.model.app.BasePlaySession;
import com.io.begstd.dice.model.wallet.WalletPromotionDto;

public interface WalletExtension extends Extension {
    default String getName() {
        return this.getClass().getName();
    }
    
    BasePlaySession minusWalletWithPromotion(WalletPromotionDto walletPromotionDto);
    
    void minusWallet(WalletPromotionDto walletPromotionDto);
}
