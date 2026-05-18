package com.io.begstd.slot.game;

import com.google.protobuf.Value;
import com.io.begstd.log.LogMessage;
import com.io.begstd.log.LogsUtils;
import com.io.begstd.slot.common.SlotGameConstant;
import com.io.begstd.slot.grpc.promotiongame.PromotionGameData;
import com.io.begstd.slot.grpc.promotiongame.PromotionGameResponse;
import com.io.begstd.slot.grpc.promotiongame.PromotionGameServiceGrpc;
import com.io.begstd.slot.grpc.promotiongame.UpdateUserPromotionGameRequest;
import com.io.begstd.slot.model.app.Promotion;
import com.io.begstd.slot.model.config.CurrencyType;
import com.io.begstd.slot.model.config.SlotMachineConfigForNormal;
import com.io.begstd.slot.repository.PromotionRepositoryService;
import io.grpc.stub.StreamObserver;
import org.lognet.springboot.grpc.GRpcService;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.util.List;

@GRpcService
public class PromotionGameGrpcServiceImpl extends PromotionGameServiceGrpc.PromotionGameServiceImplBase {
    @Autowired
    private SlotMachineConfigForNormal slotMachineConfig;
   
    @Autowired
    private PromotionRepositoryService promotionRepository;
    
 // provide getPlaySession
    @Override
    public void updateUserPromotion(UpdateUserPromotionGameRequest request, StreamObserver<PromotionGameResponse> responseObserver) {
      //log
        long lStartTimeGobal = Instant.now().toEpochMilli();
        LogMessage.LogMessageBuilder logBuilder = LogMessage.builder();
        logBuilder.cmdId("")
            .actorId("PromotionService")
            .serviceId(slotMachineConfig.serviceId())
            .psId("")
            .stateName("PromotionGameGrpcServiceImpl")
            .owner(LogMessage.OWNER_PROMOTION)
            .message(request.toString())
            .stepName("updateUserPromotion");
        
        LogsUtils.writeLogInfo(logBuilder.build());
        
        Promotion promotion = null;
        
        List<PromotionGameData> promotionDataList = request.getListPromotionDataList();
        for (PromotionGameData item: promotionDataList) {
            promotion = buildPromotionModel(item);
            if (SlotGameConstant.PROMOTION_EXPIRED_STATUS.equalsIgnoreCase(promotion.getNotifyStatus())) {
                // get promotion and update expired time
                Promotion redisPromotion = promotionRepository.get(slotMachineConfig.serviceId(), promotion.getUserId(), promotion.getCurrency());
                
                if (redisPromotion != null) {
                    redisPromotion.setExpireAt(promotion.getExpireAt());
                    promotionRepository.save(slotMachineConfig.serviceId(), redisPromotion);
                }
            } else {
                promotionRepository.save(slotMachineConfig.serviceId(), promotion);
            }
        }
        
        PromotionGameResponse.Builder builder = PromotionGameResponse.newBuilder();
        builder.setCode(0);
        responseObserver.onNext(builder.build());
        responseObserver.onCompleted();

    }
    
    private Promotion buildPromotionModel(PromotionGameData promotionData) {
        if(promotionData == null)
            return null;
        
        Promotion result = new Promotion();
        
        result.setCode(promotionData.getPromotionCode());
        result.setName(promotionData.getPromitionName());
        result.setValid(promotionData.getIsValid());
        result.setExpireAt(promotionData.getExpireAt());
        Value data = promotionData.getData().getFields().get("betid");
        result.setBetId(data != null ? data.getStringValue(): "");
        data = promotionData.getData().getFields().get("remain");
        result.setRemain(data != null ? (int)data.getNumberValue(): 0);
        data = promotionData.getData().getFields().get("total");
        result.setTotal(data != null ? (int)data.getNumberValue() : 0);
        result.setUserId(promotionData.getUserId());
        result.setNotifyStatus(promotionData.getType());
        result.setStatus(0);
        CurrencyType currType = slotMachineConfig.getAvailableCurrency(promotionData.getCurr());
        result.setCurrency(currType.name());
        
        return result;
    }
    
    
}
