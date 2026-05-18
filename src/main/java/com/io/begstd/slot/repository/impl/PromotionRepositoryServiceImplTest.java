package com.io.begstd.slot.repository.impl;

import com.io.begstd.slot.model.app.Promotion;
import com.io.begstd.slot.repository.PromotionRepositoryService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PromotionRepositoryServiceImplTest implements PromotionRepositoryService {

    
    @Override
    public void save(String serviceId, Promotion promotion) {
        log.debug("Start save data {}", promotion);
        
    }

    @Override
    public Promotion get(String serviceId, String userId, String currency) {
        Promotion promotion = null;
        log.debug("Start get data {}", userId);
        
        return promotion;
    }

    @Override
    public void remove(String serviceId, String userId, String currency) {
        log.debug("Start remove data {}", userId);
        
    }
}
