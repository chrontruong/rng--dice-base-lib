package com.io.begstd.dice.services.extension.common.impl;

import com.io.begstd.dice.model.app.BasePlaySession;
import com.io.begstd.dice.model.wallet.WalletPromotionDto;
import com.io.begstd.dice.services.extension.common.WalletExtension;
import com.io.begstd.dice.utils.GameUtils;

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
