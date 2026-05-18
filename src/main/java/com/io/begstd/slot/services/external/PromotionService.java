package com.io.begstd.slot.services.external;

import com.io.begstd.slot.model.app.Promotion;

public interface PromotionService {
    Promotion usePromotionCode(String serviceId, String commandId, String userId, String promotionCode, String prefixService, String currency);
    
    Promotion checkUserHasPromotion(String serviceId, String commandId, String userId, String prefixService, String currency);
    
    Promotion minusPromotionData(String serviceId, String psId, String userId, String promotionCode, String prefixService, String currency);
    
    Promotion reservePromotionData(String serviceId, String psId, String userId, String promotionCode, String prefixService, String currency);
    
    Promotion commitPromotionData(String serviceId, String psId, String userId, String promotionCode, String prefixService, String currency);
    
    Promotion releasePromotionData(String serviceId, String psId, String userId, String promotionCode, String prefixService, String currency);


    void updateServiceHasPromotion(String serviceId, String commandId, String prefixService);

    boolean hasPromotion();
}
