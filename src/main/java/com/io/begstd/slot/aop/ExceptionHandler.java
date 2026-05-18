package com.io.begstd.slot.aop;

import com.google.protobuf.GeneratedMessageV3;
import com.io.begstd.slot.common.SlotGameError;
import com.io.begstd.slot.exception.SlotGameException;
import com.io.begstd.slot.exception.SlotGameMessageException;
import com.io.begstd.slot.grpc.slotgame.ResponseStatusSG;
import io.grpc.stub.StreamObserver;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.lognet.springboot.grpc.GRpcGlobalInterceptor;
import org.springframework.context.annotation.Configuration;

@Aspect
@Configuration
public class ExceptionHandler extends BaseExceptionHandler {

    @Around("execution(* (@org.lognet.springboot.grpc.GRpcService *).*(..)) && args(request,responseObserver)")
    public void handlerExceptionForGRpc(ProceedingJoinPoint joinPoint, GeneratedMessageV3 request, StreamObserver<ResponseStatusSG> responseObserver) {
        try {
            joinPoint.proceed();
        } catch (SlotGameException ex) {
            logException(ex);
            sendResponseToClient(ex, responseObserver);
            if (!joinPoint.getSignature().getName().equalsIgnoreCase("leaveGame")) {
                pushErrorToClient(ex);
            }
        } catch (SlotGameMessageException exm) {
            logException(exm);
            sendResponseToClient(exm, responseObserver);
            if (!joinPoint.getSignature().getName().equalsIgnoreCase("leaveGame")) {
                pushMessageToClient(exm);
            }
        } catch (Throwable ex) {
            logException(ex);
            sendResponseToClient(new SlotGameException(SlotGameError.UNEXPECTED, null, null, null), responseObserver);
        }

    }

//    @Around("execution(@com.io.begstd.slot.annotation.Scenario * *(..))")
//    public void handlerExceptionForRtpScenario(ProceedingJoinPoint joinPoint) {
//        try {
//            joinPoint.proceed();
//        } catch (Throwable ex) {
//            logException(ex);
//        }
//    }
}
