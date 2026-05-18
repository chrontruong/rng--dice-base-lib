package com.io.begstd.slot.services.extension.common.impl;

import com.io.begstd.log.LogMessage;
import com.io.begstd.slot.command.SpinCmd;
import com.io.begstd.slot.model.app.Promotion;
import com.io.begstd.slot.model.app.UserInfo;
import com.io.begstd.slot.repository.PromotionRepositoryService;
import com.io.begstd.slot.services.extension.common.PromotionExtension;
import com.io.begstd.slot.utils.GameUtils;

public class PromotionExtensionImpl implements PromotionExtension {
    
    @Override
    public Promotion getPromotion(PromotionRepositoryService promotionRepository, LogMessage.LogMessageBuilder logBuilder, String serviceId, UserInfo userInfo, SpinCmd cmd, String commandId) {
        return GameUtils.getPromotion(promotionRepository, logBuilder, serviceId, userInfo.userId(), userInfo.userType(), cmd, commandId, userInfo.currency());
    }
}
