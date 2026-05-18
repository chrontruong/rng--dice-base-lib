package com.io.begstd.slot.aop;

import com.io.begstd.log.LogMessage;
import com.io.begstd.log.LogsUtils;
import com.io.begstd.slot.exception.SlotGameException;
import com.io.begstd.slot.exception.SlotGameMessageException;
import com.io.begstd.slot.grpc.slotgame.ResponseStatusSG;
import com.io.begstd.slot.model.app.CommandIdHistory;
import com.io.begstd.slot.model.config.ISlotMachineConfig;
import com.io.begstd.slot.model.config.SlotConfigMode;
import com.io.begstd.slot.services.external.PlayerViewStoreService;
import com.io.begstd.slot.services.internal.impl.QueueHistoryServiceImpl;
import com.io.begstd.slot.utils.ConfigManager;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;

public abstract class BaseExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(BaseExceptionHandler.class);
//    @Autowired
//    private MessageSourceAccessor messageSourceAccessor;

    @Autowired
    private PlayerViewStoreService playerViewStoreService;

    //    @Autowired
//    private UserService userService;
    @Autowired
    private QueueHistoryServiceImpl<String, CommandIdHistory> queueHistoryUtils;

    @Autowired
    private ConfigManager configManager;

    protected void logException(Throwable ex) {
        //log builder
        LogMessage.LogMessageBuilder logBuilder = LogMessage.builder();
        logBuilder
                .cmdId("")
                .actorId("")
                .serviceId("")
                .psId("")
                .stateName("")
                .stepName("").owner(LogMessage.OWNER_GRPC)
                .message("Error - Throwable " + ex.getMessage())
                .timeExe(0);
        LogsUtils.writeLogError(logBuilder.build());
        log.error("ERROR Throwable: " + ex.getMessage(), ex);
    }

    protected void logException(SlotGameException ex) {
//        String errorMessage = messageSourceAccessor.getMessage(ex.getMessage(),
//                ex.getParameters().toArray());
//        log.error("ERROR SlotGameException: ", errorMessage, ex);
        LogMessage.LogMessageBuilder logBuilder = LogMessage.builder();
        logBuilder
                .cmdId("")
                .actorId("")
                .serviceId("")
                .psId("")
                .stateName("")
                .message("Error - SlotGameException " + ex.getMessage())
                .stepName("").owner(LogMessage.OWNER_GRPC)
                .timeExe(0);
        LogsUtils.writeLogException(logBuilder.build(), ex);
    }

    protected void sendResponseToClient(SlotGameException slotGameException, StreamObserver<ResponseStatusSG> responseObserver) {
//        log.info("Start sendResponseToClient SlotGameException");
        try {
            ResponseStatusSG resultRes = ResponseStatusSG.newBuilder().setC(slotGameException.getErrorCode()).build();
            responseObserver.onNext(resultRes);
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
//        log.info("End sendResponseToClient SlotGameException");
    }

    protected void pushErrorToClient(SlotGameException slotGameException) {
        try {
            ISlotMachineConfig slotMachineConfig = (ISlotMachineConfig) configManager.getConfigMain(SlotConfigMode.NORMAL);
//        log.info("Start pushErrorToClient SlotGameException");
            boolean resUpdate = false;
            if (slotGameException.getParameters().size() == 3) {
                resUpdate = playerViewStoreService.pushError(String.valueOf(slotMachineConfig.serviceId()),
                        slotGameException.getParameters().get(0), //userid
                        slotGameException.getParameters().get(1), //userType
                        slotGameException.getParameters().get(2), //cmdId
                        Arrays.asList(slotGameException.getErrorCode()));
            } else {
                resUpdate = playerViewStoreService.pushErrorHasMeta(String.valueOf(slotMachineConfig.serviceId()),
                        slotGameException.getParameters().get(0), //userid
                        slotGameException.getParameters().get(1), //userType
                        slotGameException.getParameters().get(2), //cmdId
                        Arrays.asList(slotGameException.getErrorCode()),
                        slotGameException.getParameters().get(3)); //meta data = 0016);
            }
//        log.error("Push Error to PlayerViewStore:" + resUpdate);
//        log.error("Error code:" + slotGameException.getErrorCode() + " command: " + slotGameException.getParameters().get(1));
            LogMessage.LogMessageBuilder logBuilder = LogMessage.builder();
            logBuilder
                    .cmdId(slotGameException.getParameters().get(1))
                    .actorId(slotGameException.getParameters().get(0))
                    .serviceId(slotMachineConfig.serviceId())
                    .psId("")
                    .stateName("PushError")
                    .stepName("pushErrorToPVS").owner(LogMessage.OWNER_GAME)
                    .message(Arrays.asList(slotGameException.getErrorCode()) + " send to client: " + resUpdate)
                    .timeExe(0);
            LogsUtils.writeLogDebug(logBuilder.build());
            //save error of commandId in redis
            queueHistoryUtils.insert(slotGameException.getParameters().get(2), new CommandIdHistory(slotMachineConfig.serviceId(), slotGameException.getParameters().get(2),
                    slotGameException.getParameters().get(0), false, slotGameException.getErrorCode()));
//        userService.saveCommandIdInRedis(new CommandIdHistory(slotMachineConfig.serviceId(), slotGameException.getParameters().get(2), 
//            slotGameException.getParameters().get(0), false, slotGameException.getErrorCode()));
            //save playsession in redis after updated error code
            //TODO network v3
//        PlaySession ps = (PlaySession)slotGameException.getParameters().get(2);
//        if (ps != null) {
//            PlaySession psUpdated = GameUtils.addErrorCodeToPlaySession(ps, slotGameException.getErrorCode());
//            userService.savePlaySession(psUpdated);
//            playerViewStoreService.updateState(psUpdated);
//        }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    protected void sendResponseToClient(SlotGameMessageException slotGameException, StreamObserver<ResponseStatusSG> responseObserver) {
//        log.info("Start sendResponseToClient SlotGameMessageException");
        try {
            ResponseStatusSG resultRes = ResponseStatusSG.newBuilder().setC(slotGameException.getMessageCode()).build();
            responseObserver.onNext(resultRes);
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
//        log.info("End sendResponseToClient SlotGameMessageException");
    }

    protected void pushMessageToClient(SlotGameMessageException slotGameException) {
        try {
            ISlotMachineConfig slotMachineConfig = (ISlotMachineConfig) configManager.getConfigMain(SlotConfigMode.NORMAL);
//        log.info("Start pushMessageToClient SlotGameMessageException");
            boolean resUpdate = this.playerViewStoreService.pushMessage(String.valueOf(slotMachineConfig.serviceId()),
                    slotGameException.getParameters().get(0),
                    slotGameException.getParameters().get(1),
                    slotGameException.getParameters().get(2),
                    slotGameException.getMessageCode());
//        log.error("Push Message to PlayerViewStore:" + resUpdate);
//        log.error("Message code:" + slotGameException.getMessageCode() + " command: "+ slotGameException.getParameters().get(1));
            LogMessage.LogMessageBuilder logBuilder = LogMessage.builder();
            logBuilder
                    .cmdId(slotGameException.getParameters().get(1))
                    .actorId(slotGameException.getParameters().get(0))
                    .serviceId(slotMachineConfig.serviceId())
                    .psId("")
                    .stateName("Exception")
                    .stepName("pushMessage").owner(LogMessage.OWNER_GAME)
                    .message("Message code:" + Arrays.asList(slotGameException.getMessageCode()) + ". Result PVS - " + resUpdate)
                    .timeExe(0);
            LogsUtils.writeLogDebug(logBuilder.build());

            //save error of commandId in memory
            queueHistoryUtils.insert(slotGameException.getParameters().get(2), new CommandIdHistory(slotMachineConfig.serviceId(), slotGameException.getParameters().get(2),
                    slotGameException.getParameters().get(0), false, slotGameException.getMessageCode()));
            //save error of commandId in redis
//        userService.saveCommandIdInRedis(new CommandIdHistory(slotMachineConfig.serviceId(), slotGameException.getParameters().get(1), 
//            slotGameException.getParameters().get(0), false, slotGameException.getMessageCode()));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

}
