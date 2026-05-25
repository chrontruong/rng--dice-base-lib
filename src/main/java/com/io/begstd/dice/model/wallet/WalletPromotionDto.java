package com.io.begstd.dice.model.wallet;

import com.io.begstd.log.LogMessage;
import com.io.begstd.dice.model.app.BasePlaySession;
import com.io.begstd.dice.model.app.Promotion;
import com.io.begstd.dice.model.app.UserInfo;
import com.io.begstd.dice.model.domain.Money;
import com.io.begstd.dice.repository.PromotionRepositoryService;
import com.io.begstd.dice.services.external.PromotionService;
import com.io.begstd.dice.services.internal.WalletTrialModeService;
import com.io.begstd.wallet.service.WalletService;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@AllArgsConstructor
@Getter
@Setter
@Accessors(fluent = true)
public class WalletPromotionDto {
    private BasePlaySession basePlaySession;
    private LogMessage.LogMessageBuilder logBuilder;
    private int totalCredit;
    private String serviceCode;
    private String serviceName;
    private String prefixService;
    private WalletTrialModeService walletTrialModeService;
    private WalletService walletService;
    private Money bettingTotal;
    private boolean isTrialMode;
    private Promotion promotion;
    private PromotionRepositoryService promotionRepository;
    private PromotionService promotionService;
    private UserInfo userInfo;
}
