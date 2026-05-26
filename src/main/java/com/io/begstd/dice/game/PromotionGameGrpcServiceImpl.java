package com.io.begstd.dice.game;

import com.google.protobuf.Value;
import com.io.begstd.log.LogMessage;
import com.io.begstd.log.LogsUtils;
import com.io.begstd.dice.model.app.Promotion;
import com.io.begstd.dice.model.config.CurrencyType;
import com.io.begstd.dice.model.config.DiceMachineConfig;
import com.io.begstd.dice.repository.PromotionRepositoryService;
import com.io.begstd.dice.common.DiceGameConstant;
import com.io.begstd.dice.grpc.promotiongame.PromotionDiceGameData;
import com.io.begstd.dice.grpc.promotiongame.PromotionDiceGameResponse;
import com.io.begstd.dice.grpc.promotiongame.PromotionDiceGameServiceGrpc;
import com.io.begstd.dice.grpc.promotiongame.UpdateUserPromotionDiceGameRequest;
import io.grpc.stub.StreamObserver;
import org.lognet.springboot.grpc.GRpcService;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.util.List;

@GRpcService
public class PromotionGameGrpcServiceImpl extends PromotionDiceGameServiceGrpc.PromotionDiceGameServiceImplBase {
    @Autowired
    private DiceMachineConfig slotMachineConfig;

    @Autowired
    private PromotionRepositoryService promotionRepository;

 // provide getPlaySession
    @Override
    public void updateUserPromotion(UpdateUserPromotionDiceGameRequest request, StreamObserver<PromotionDiceGameResponse> responseObserver) {
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

        List<PromotionDiceGameData> promotionDataList = request.getListPromotionDataList();
        for (PromotionDiceGameData item: promotionDataList) {
            promotion = buildPromotionModel(item);
            if (DiceGameConstant.PROMOTION_EXPIRED_STATUS.equalsIgnoreCase(promotion.getNotifyStatus())) {
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

        PromotionDiceGameResponse.Builder builder = PromotionDiceGameResponse.newBuilder();
        builder.setCode(0);
        responseObserver.onNext(builder.build());
        responseObserver.onCompleted();

    }

    private Promotion buildPromotionModel(PromotionDiceGameData promotionData) {
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
