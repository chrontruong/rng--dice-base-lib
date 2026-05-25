package com.io.begstd.dice.services.external.impl;

import com.io.begstd.dice.model.app.Promotion;
import com.io.begstd.dice.services.external.PromotionService;

public class PromotionServiceTestImpl implements PromotionService {

    @Override
    public Promotion usePromotionCode(String serviceId, String commandId, String userId, String promotionCode, String prefixService, String currency) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Promotion checkUserHasPromotion(String serviceId, String commandId, String userId, String prefixService, String currency) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Promotion minusPromotionData(String serviceId, String psId, String userId, String promotionCode, String prefixService, String currency) {
        // TODO Auto-generated method stub
        return null;
    }
    public Promotion reservePromotionData(String serviceId, String psId, String userId, String promotionCode, String prefixService, String currency) {
        return null;
    }
    
    public Promotion commitPromotionData(String serviceId, String psId, String userId, String promotionCode, String prefixService, String currency) {
        return null;
    }
    
    public Promotion releasePromotionData(String serviceId, String psId, String userId, String promotionCode, String prefixService, String currency) {
        return null;
    }


    @Override
    public void updateServiceHasPromotion(String serviceId, String commandId, String prefixService) {

    }

    @Override
    public boolean hasPromotion() {
        return false;
    }
}
