package com.io.begstd.slot.services.extension.common;

import com.io.begstd.extension.Extension;
import com.io.begstd.log.LogMessage;
import com.io.begstd.slot.command.SpinCmd;
import com.io.begstd.slot.model.app.Promotion;
import com.io.begstd.slot.model.app.UserInfo;
import com.io.begstd.slot.repository.PromotionRepositoryService;

public interface PromotionExtension extends Extension {
    default String getName() {
        return this.getClass().getName();
    }
    Promotion getPromotion(PromotionRepositoryService promotionRepository, LogMessage.LogMessageBuilder logBuilder,
                           String serviceId, UserInfo userInfo, SpinCmd cmd, String commandId);
}
