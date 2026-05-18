package com.io.begstd.slot.services.extension.common;

import com.io.begstd.extension.Extension;
import com.io.begstd.slot.model.app.BasePlaySession;
import com.io.begstd.slot.model.wallet.WalletPromotionDto;

public interface WalletExtension extends Extension {
    default String getName() {
        return this.getClass().getName();
    }
    
    BasePlaySession minusWalletWithPromotion(WalletPromotionDto walletPromotionDto);
    
    void minusWallet(WalletPromotionDto walletPromotionDto);
}
