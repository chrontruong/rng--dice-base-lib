package com.io.begstd.slot.services.extension.common.impl;

import com.io.begstd.slot.model.app.BasePlaySession;
import com.io.begstd.slot.model.wallet.WalletPromotionDto;
import com.io.begstd.slot.services.extension.common.WalletExtension;
import com.io.begstd.slot.utils.GameUtils;

public class WalletExtensionImpl implements WalletExtension {
    
    @Override
    public BasePlaySession minusWalletWithPromotion(WalletPromotionDto walletPromotionDto) {
        return GameUtils.minusWalletWithPromotion(walletPromotionDto);
    }
    
    @Override
    public void minusWallet(WalletPromotionDto walletPromotionDto) {
        GameUtils.minusWallet(walletPromotionDto);
    }
}
