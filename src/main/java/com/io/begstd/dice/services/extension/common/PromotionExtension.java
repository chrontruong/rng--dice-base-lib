package com.io.begstd.dice.services.extension.common;

import com.io.begstd.extension.Extension;
import com.io.begstd.log.LogMessage;
import com.io.begstd.dice.model.app.Promotion;
import com.io.begstd.dice.model.app.UserInfo;
import com.io.begstd.dice.repository.PromotionRepositoryService;
import com.io.begstd.dice.command.SpinCmd;

public interface PromotionExtension extends Extension {
    default String getName() {
        return this.getClass().getName();
    }
    Promotion getPromotion(PromotionRepositoryService promotionRepository, LogMessage.LogMessageBuilder logBuilder,
                           String serviceId, UserInfo userInfo, SpinCmd cmd, String commandId);
}
