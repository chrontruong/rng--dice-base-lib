package com.io.begstd.slot.repository;

import com.io.begstd.slot.model.app.Promotion;

public interface PromotionRepositoryService {

    void save(String serviceId, Promotion promotion);

    Promotion get(String serviceId, String userId, String currency);

    void remove(String serviceId, String userId, String currency);
}
