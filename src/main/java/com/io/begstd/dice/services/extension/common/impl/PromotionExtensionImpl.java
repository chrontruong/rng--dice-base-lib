package com.io.begstd.dice.services.extension.common.impl;

import com.io.begstd.log.LogMessage;
import com.io.begstd.dice.model.app.Promotion;
import com.io.begstd.dice.model.app.UserInfo;
import com.io.begstd.dice.repository.PromotionRepositoryService;
import com.io.begstd.dice.services.extension.common.PromotionExtension;
import com.io.begstd.dice.utils.GameUtils;
import com.io.begstd.dice.command.SpinCmd;

public class PromotionExtensionImpl implements PromotionExtension {
    
    @Override
    public Promotion getPromotion(PromotionRepositoryService promotionRepository, LogMessage.LogMessageBuilder logBuilder, String serviceId, UserInfo userInfo, SpinCmd cmd, String commandId) {
        return GameUtils.getPromotion(promotionRepository, logBuilder, serviceId, userInfo.userId(), userInfo.userType(), cmd, commandId, userInfo.currency());
    }
}
