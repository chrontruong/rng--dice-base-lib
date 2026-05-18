package com.io.begstd.slot.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.io.begstd.log.LogMessage;
import com.io.begstd.log.LogsUtils;
import com.io.begstd.slot.command.SpinCmd;
import com.io.begstd.slot.common.SlotGameConstant;
import com.io.begstd.slot.common.SlotGameError;
import com.io.begstd.slot.common.SlotGameMessage;
import com.io.begstd.slot.exception.SlotGameException;
import com.io.begstd.slot.exception.SlotGameMessageException;
import com.io.begstd.slot.factory.GameRuleFactory;
import com.io.begstd.slot.model.app.*;
import com.io.begstd.slot.model.app.BasePlaySession.BasePlaySessionBuilder;
import com.io.begstd.slot.model.config.ICommonSlotConfig;
import com.io.begstd.slot.model.config.ISlotMachineConfig;
import com.io.begstd.slot.model.config.SlotConfigMode;
import com.io.begstd.slot.model.domain.Money;
import com.io.begstd.slot.model.gamerule.*;
import com.io.begstd.slot.model.playsession.ISymbolBonusGame;
import com.io.begstd.slot.model.wallet.WalletPromotionDto;
import com.io.begstd.slot.repository.PromotionRepositoryService;
import com.io.begstd.slot.services.external.JackpotService;
import com.io.begstd.slot.services.external.PlayerViewStoreService;
import com.io.begstd.slot.services.internal.JackpotTrialModeService;
import com.io.begstd.slot.services.internal.UserService;
import com.io.begstd.slot.services.internal.WalletTrialModeService;
import com.io.begstd.slot.services.internal.impl.NormalGameServiceImpl;
import com.io.begstd.slot.services.internal.impl.QueueHistoryServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.javatuples.Pair;
import org.springframework.core.env.Environment;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.stream.Collectors;

@Slf4j
public class GameUtils {
    private static final NumberFormat DOUBLE_FORMATTER = new DecimalFormat("#.##");

    private static IPlaySessionClass playSessionClass;
    private static IPlaySessionClass getPlaySessionClass() {
        if (playSessionClass == null) {
            playSessionClass = BeanUtils.getBean(IPlaySessionClass.class);
        }
        return playSessionClass;
    }

    private static String buildVersion;
    public static String getBuildVersion() {
        if (buildVersion == null) {
            try (InputStream inputStream =
                         GameUtils.class.getClassLoader().getResourceAsStream("META-INF/MANIFEST.MF")) {

                if (inputStream != null) {
                    Manifest manifest = new Manifest(inputStream);
                    Attributes attr = manifest.getMainAttributes();
                    buildVersion = attr.getValue("Implementation-Version");
                } else {
                    buildVersion = "1.0";
                }

            } catch (Exception e) {
                buildVersion = "1.1";
            }
        }
        return buildVersion;
    }

    public static void checkingCommandIdInQueue(UserService userService, String commandId, UserInfo userInfo, String serviceId) {
        
        QueueHistoryServiceImpl<String, CommandIdHistory> commandIdList = BeanUtils.getBean(QueueHistoryServiceImpl.class);
        if (commandIdList.isExist(commandId)) {
            CommandIdHistory commandItem  = commandIdList.getValue(commandId);
            if (commandItem.getUserId().equalsIgnoreCase(userInfo.userId())) {
                log.error("CommandId has handled :" + commandId); 
                PlayerViewStoreService pvstoreImpl = BeanUtils.getBean(PlayerViewStoreService.class);
                BasePlaySession basePlaySession = userService.getPlaySession(serviceId, userInfo);
                BasePlaySession basePlaySessionUpdated = null;
                if (commandItem.isSuccess()) {
                    //push message
                    log.error("Succession of commandId has handled:" + commandId);
                    //throw new SlotGameMessageException(SlotGameMessage.HAVE_COMMANDID, userId, commandId);
                    if (basePlaySession != null) {
                        basePlaySessionUpdated = GameUtils.addErrorCodeToPlaySession(basePlaySession, SlotGameMessage.HAVE_COMMANDID.getMessageCode());
                        pvstoreImpl.updateState(GameUtils.updateCommandIdPlaySession(basePlaySessionUpdated, commandId));
                    }
                    throw new SlotGameMessageException(SlotGameMessage.HAVE_COMMANDID, userInfo.userId(), "", commandId);
                } else {
                    //push error
                    if (commandItem.getErrorCode().equals(SlotGameMessage.HAVE_PLAYSESSION.getMessageCode())) {
//                        throw new SlotGameMessageException(SlotGameMessage.HAVE_PLAYSESSION, userId, commandId);
                        if (basePlaySession != null) {
                            basePlaySessionUpdated = GameUtils.addErrorCodeToPlaySession(basePlaySession, SlotGameMessage.HAVE_PLAYSESSION.getMessageCode());
                            pvstoreImpl.updateState(GameUtils.updateCommandIdPlaySession(basePlaySessionUpdated, commandId));
                        }
                    } else {
                        log.error("Error of commandId has handled:" + commandId);
                        if (basePlaySession != null) {
                            basePlaySessionUpdated = GameUtils.addErrorCodeToPlaySession(basePlaySession, commandItem.getErrorCode());
                        }
                        throw new SlotGameException(SlotGameError.getValue(commandItem.getErrorCode()), userInfo.userId(), "", commandId);
                    }
                }
                if (basePlaySessionUpdated != null) {
                    userService.savePlaySession(basePlaySessionUpdated);
                }
            }
        }
        
    }
    public static void checkingCommandId(UserService userService, String commandId, String userId, String serviceId) throws SlotGameException, SlotGameMessageException {
        
        CommandIdHistory commandItem = userService.getCommandIdInRedis(commandId, serviceId);
        if (commandItem != null) {
          //get data push
            log.error("CommandId has handled :" + commandId); 
            PlayerViewStoreService pvstoreImpl = BeanUtils.getBean(PlayerViewStoreService.class);
            BasePlaySession basePlaySession = userService.getPlaySession(serviceId, UserInfo.builder().userId(userId).build());
            BasePlaySession basePlaySessionUpdated = null;
            if (commandItem.getUserId().equalsIgnoreCase(userId)) {
                if (commandItem.isSuccess()) {
                    //push message
                    log.error("Succession of commandId has handled:" + commandId);
                    //throw new SlotGameMessageException(SlotGameMessage.HAVE_COMMANDID, userId, commandId);
                    if (basePlaySession != null) {
                        basePlaySessionUpdated = GameUtils.addErrorCodeToPlaySession(basePlaySession, SlotGameMessage.HAVE_COMMANDID.getMessageCode());
                        pvstoreImpl.updateState(GameUtils.updateCommandIdPlaySession(basePlaySessionUpdated, commandId));
                    }
//                    throw new SlotGameMessageException(SlotGameMessage.HAVE_COMMANDID, userId, commandId);
                } else {
                    //push error
                    if (commandItem.getErrorCode().equals(SlotGameMessage.HAVE_PLAYSESSION.getMessageCode())) {
//                        throw new SlotGameMessageException(SlotGameMessage.HAVE_PLAYSESSION, userId, commandId);
                        if (basePlaySession != null) {
                            basePlaySessionUpdated = GameUtils.addErrorCodeToPlaySession(basePlaySession, SlotGameMessage.HAVE_PLAYSESSION.getMessageCode());
                            pvstoreImpl.updateState(GameUtils.updateCommandIdPlaySession(basePlaySessionUpdated, commandId));
                        }
                    } else {
                        log.error("Error of commandId has handled:" + commandId);
                        if (basePlaySession != null) {
                            basePlaySessionUpdated = GameUtils.addErrorCodeToPlaySession(basePlaySession, commandItem.getErrorCode());
                        }
                        throw new SlotGameException(SlotGameError.getValue(commandItem.getErrorCode()), userId, "", commandId);
                    }
                }
            }
            if (basePlaySessionUpdated != null) {
                userService.savePlaySession(basePlaySessionUpdated);
            }
        }
    }

    public static BasePlaySession addErrorCodeToPlaySession(BasePlaySession basePlaySession, String errorCode) {
        BasePlaySessionBuilder psBuilder = basePlaySession.toBuilder();
        psBuilder.addErrorCodeList(errorCode);
        return psBuilder.build();
        
    }
    
    public static BasePlaySession updateCommandIdPlaySession(BasePlaySession basePlaySession, String commandId) {
        BasePlaySessionBuilder psBuilder = basePlaySession.toBuilder();
        psBuilder.commandId(commandId);
        return psBuilder.build();
        
    }
    
    public static Money calculateBettingTotal(List<BettingLine> betPerLineList) {
        Money bettingAmount = Money.ZERO;
        for (BettingLine item : betPerLineList) {
            bettingAmount = bettingAmount.add(item.betMoney());
        }
        return bettingAmount;
    }
    
    public static String printLine(List<Symbol> rowMatchBetLine) {
        StringBuffer resultStr = new StringBuffer();
        for (Symbol item: rowMatchBetLine) {
            if(item  != null) { 
                resultStr.append(item.code()+" ");  
            }   
        }
        return resultStr.toString();
    }
    
    public static Map<String, Money> distributeJackPots(ISlotMachineConfig slotMachineConfigNormal, DenominationLevel denominationLevel,
            Money totalBet) {
        Map<String, Money> jackpotGroup = new HashMap<>();

        for (InitJackpotChild jpItem : slotMachineConfigNormal.initJackpotList()) {
            Money plusJackpot = totalBet.multiply(jpItem.progressive()).divide(slotMachineConfigNormal.totalPercent());
            if (plusJackpot.isGreaterThan(Money.ZERO)) {
                jackpotGroup.put(denominationLevel.jackpotID() + jpItem.code(), plusJackpot);
            }
        }
        return jackpotGroup;
    }
    
    public static int randomValueInRange(List<Integer> rangePercent,int idxRandom) {
        for (int i = 0; i < rangePercent.size(); i++ ) {
            if(rangePercent.get(i) > idxRandom) {
                return i;
            }
        }
        log.error(" ERRR randomValueInRange:" +idxRandom);
        return -1;
    }
    

    public static Pair<String, String> buildTotalBet( String totalBet){
        final char[] totalBetArray = totalBet.toCharArray();    
        return Pair.with(totalBetArray[0] + "0", String.valueOf(totalBetArray[1])); 
    }   
    public static String getBetId( String totalBet){
        return buildTotalBet(totalBet).getValue0(); 
    }   
    public static String getExtraBetId( String totalBet){
        return buildTotalBet(totalBet).getValue1(); 
    }     
    
    public static List<String> convertBonusMatrixIDToList(DataCell<ISymbolBonusGame>[][] matrix, List<Integer> tableFormat) {
        List<String> matrixCode1D = new ArrayList<>();
        if (matrix != null) {
            for (int reelIdx = 0, reeSize = tableFormat.size(); reelIdx < reeSize; reelIdx++) {
                for (int rowIdx = 0, rowSize = matrix.length; rowIdx < rowSize; rowIdx++) {
                    ISymbolBonusGame symbol = matrix[rowIdx][reelIdx].symbol();
                    if (symbol != null) {
                        matrixCode1D.add(String.valueOf(symbol.id()));
                    } else {
                        matrixCode1D.add("");
                    }
                }
            }
        }
        return matrixCode1D;
    }
    
    public static List<String> convertBonusMatrixCodeToList(DataCell<ISymbolBonusGame>[][] matrix, List<Integer> tableFormat) {
        List<String> matrixCode1D = new ArrayList<>();
        if (matrix != null) {
            for (int reelIdx = 0, reeSize = tableFormat.size(); reelIdx < reeSize; reelIdx++) {
                for (int rowIdx = 0, rowSize = matrix.length; rowIdx < rowSize; rowIdx++) {
                    ISymbolBonusGame symbol = matrix[rowIdx][reelIdx].symbol();
                    if (symbol != null) {
                        matrixCode1D.add(symbol.code());
                    } else {
                        matrixCode1D.add("");
                    }
                }
            }
        }
        return matrixCode1D;
    }
    public static List<Integer> convertBonusMatrixToList(DataCell<ISymbolBonusGame>[][] matrix, List<Integer> tableFormat) {
        List<Integer> matrixCode1D = new ArrayList<>();
        if (matrix != null) {
            for (int reelIdx = 0, reeSize = tableFormat.size(); reelIdx < reeSize; reelIdx++) {
                for (int rowIdx = 0, rowSize = matrix.length; rowIdx < rowSize; rowIdx++) {
                    ISymbolBonusGame symbol = matrix[rowIdx][reelIdx].symbol();
                    matrixCode1D.add(symbol.id());
                }
            }
        }
        return matrixCode1D;
    }
    
    public static List<Float> convertBonusMatrixToListFloatValue(DataCell<ISymbolBonusGame>[][] matrix, List<Integer> tableFormat) {
        List<Float> matrixCode1D = new ArrayList<>();
        if (matrix != null) {
            for (int reelIdx = 0, reeSize = tableFormat.size(); reelIdx < reeSize; reelIdx++) {
                for (int rowIdx = 0, rowSize = matrix.length; rowIdx < rowSize; rowIdx++) {
                    ISymbolBonusGame symbol = matrix[rowIdx][reelIdx].symbol();
                    matrixCode1D.add(symbol.value());
                }
            }
        }
        return matrixCode1D;
    }
    
    public static List<Integer> convertBonusMatrixToListIntValue(DataCell<ISymbolBonusGame>[][] matrix, List<Integer> tableFormat) {
        List<Integer> matrixCode1D = new ArrayList<>();
        if (matrix != null) {
            for (int reelIdx = 0, reeSize = tableFormat.size(); reelIdx < reeSize; reelIdx++) {
                for (int rowIdx = 0, rowSize = matrix.length; rowIdx < rowSize; rowIdx++) {
                    ISymbolBonusGame symbol = matrix[rowIdx][reelIdx].symbol();
                    matrixCode1D.add((int)symbol.value());
                }
            }
        }
        return matrixCode1D;
    }
    
    public static List<String> convertBonusMatrixToListValue(DataCell<ISymbolBonusGame>[][] matrix, List<Integer> tableFormat) {
        List<String> matrixCode1D = new ArrayList<>();
        if (matrix != null) {
            for (int reelIdx = 0, reeSize = tableFormat.size(); reelIdx < reeSize; reelIdx++) {
                for (int rowIdx = 0, rowSize = matrix.length; rowIdx < rowSize; rowIdx++) {
                    ISymbolBonusGame symbol = matrix[rowIdx][reelIdx].symbol();
                    if (symbol != null) {
                        matrixCode1D.add(String.valueOf(symbol.value()));
                    } else {
                        matrixCode1D.add("");
                    }
                }
            }
        }
        return matrixCode1D;
    }
    
    public static void minusWallet(WalletPromotionDto walletProDto) {
        
        long lStartTime = Instant.now().toEpochMilli();
        if(walletProDto.isTrialMode()) {
            String strResultCode = walletProDto.walletTrialModeService().minusWalletAmount(walletProDto.basePlaySession(), walletProDto.bettingTotal().value().doubleValue(),
                walletProDto.totalCredit(), walletProDto.serviceCode() + ":"+ walletProDto.serviceName(), walletProDto.prefixService());
            walletProDto.logBuilder().stepName("Minus wallet trial").owner(LogMessage.OWNER_WALLET)
                .message("Minus wallet for trial mode: "+strResultCode +" - isTrialMode: "+ walletProDto.isTrialMode())
                .timeExe(Instant.now().toEpochMilli() - lStartTime);
            if (!"0".equals(strResultCode)) {
                walletProDto.logBuilder().stepName("Minus wallet").owner(LogMessage.OWNER_WALLET).message("ERROR: Minus wallet "+ strResultCode)
                .timeExe(Instant.now().toEpochMilli() - lStartTime);
                LogsUtils.writeLogError(walletProDto.logBuilder().build());
                
                if ("2005".equals(strResultCode)) {
                    throw new SlotGameException(SlotGameError.MONEY_NOT_ENOUGH, walletProDto.basePlaySession().userId(), walletProDto.basePlaySession().userType(), walletProDto.basePlaySession().commandId());
                } else {
                    throw new SlotGameException(SlotGameError.UNEXPECTED, walletProDto.basePlaySession().userId(), walletProDto.basePlaySession().userType(), walletProDto.basePlaySession().commandId(),
                            String.format("Error in class %s call to method minusWalletAmount with error code %s", NormalGameServiceImpl.class, strResultCode));
                }
            }
        } else {
            // subtract player's wallet
            String strResultCode = walletProDto.walletService().minusWalletAmount(walletProDto.basePlaySession(), walletProDto.bettingTotal().value().doubleValue(),
                walletProDto.totalCredit(), walletProDto.serviceCode() + ":"+ walletProDto.serviceName(), walletProDto.prefixService());
            
            if (!"0".equals(strResultCode)) {
                walletProDto.logBuilder().stepName("Minus wallet").owner(LogMessage.OWNER_WALLET)
                    .message("ERROR: Minus wallet "+ strResultCode)
                    .timeExe(Instant.now().toEpochMilli() - lStartTime);
                LogsUtils.writeLogError(walletProDto.logBuilder().build());
                
                if (!"0".equals(strResultCode)) {
                    switch (strResultCode) {
                        case "2001":
                            throw new SlotGameException(SlotGameError.WALLET_USERID_NULL, walletProDto.basePlaySession().userId(), walletProDto.basePlaySession().userType(), walletProDto.basePlaySession().commandId());
                        case "2004":
                            throw new SlotGameException(SlotGameError.WALLET_MONEY_INVALID, walletProDto.basePlaySession().userId(), walletProDto.basePlaySession().userType(), walletProDto.basePlaySession().commandId());
                        case "2005":
                            throw new SlotGameException(SlotGameError.MONEY_NOT_ENOUGH, walletProDto.basePlaySession().userId(), walletProDto.basePlaySession().userType(), walletProDto.basePlaySession().commandId());
                        case "2003":
                        case "2006":
                            throw new SlotGameException(SlotGameError.WALLET_NOTFOUND_USER, walletProDto.basePlaySession().userId(), walletProDto.basePlaySession().userType(), walletProDto.basePlaySession().commandId());
                        case "2007":
                            throw new SlotGameException(SlotGameError.WALLET_LOCKED_USER, walletProDto.basePlaySession().userId(), walletProDto.basePlaySession().userType(), walletProDto.basePlaySession().commandId());
                        case "2008":
                            throw new SlotGameException(SlotGameError.WALLET_CANNOT_CONNECT, walletProDto.basePlaySession().userId(), walletProDto.basePlaySession().userType(), walletProDto.basePlaySession().commandId());
                        case "2009":
                            throw new SlotGameException(SlotGameError.WALLET_INVALID_ACTION, walletProDto.basePlaySession().userId(), walletProDto.basePlaySession().userType(), walletProDto.basePlaySession().commandId());
                        case "2408":
                            throw new SlotGameException(SlotGameError.WALLET_TIMEOUT, walletProDto.basePlaySession().userId(), walletProDto.basePlaySession().userType(), walletProDto.basePlaySession().commandId());
                        case "2500":
                            throw new SlotGameException(SlotGameError.WALLET_SERVER_ERROR, walletProDto.basePlaySession().userId(), walletProDto.basePlaySession().userType(), walletProDto.basePlaySession().commandId());
                        case "29999":
                            throw new SlotGameException(SlotGameError.WALLET_NO_RESPONSE, walletProDto.basePlaySession().userId(), walletProDto.basePlaySession().userType(), walletProDto.basePlaySession().commandId());
                        default:
                            throw new SlotGameException(SlotGameError.WALLET_UNEXPECTED, walletProDto.basePlaySession().userId(), walletProDto.basePlaySession().userType(), walletProDto.basePlaySession().commandId(),
                                String.format("Error in class %s call to method minusWalletAmount with error code %s", NormalGameServiceImpl.class, strResultCode));
                    }
                }
            }
            walletProDto.logBuilder().stepName("Minus wallet").owner(LogMessage.OWNER_WALLET)
                .message("Passed - Minus wallet in normal model"+ strResultCode)
                .timeExe(Instant.now().toEpochMilli() - lStartTime);
        }
        LogsUtils.writeLogDebug(walletProDto.logBuilder().build());
    }
    
    public static String progressiveJackpot(String userId, String commandId, LogMessage.LogMessageBuilder logBuilder, JackpotService jackpotService, 
            JackpotTrialModeService jackpotTrialModeService, Map<String, Money> progressiveJackpotGroup, boolean isTrialMode) {
        long lStartTime = Instant.now().toEpochMilli();
        logBuilder.owner(LogMessage.OWNER_JACKPOT);
        Map<String, Money> plusJackpotInfo = null;
        if (!progressiveJackpotGroup.isEmpty()) {
            if(!isTrialMode) {
//                boolean resPlus = jackpotService.plusMultipleJackpots(commandId, progressiveJackpotGroup);
                plusJackpotInfo = jackpotService.plusMultipleJackpotsWithAmount(commandId, progressiveJackpotGroup);
                logBuilder.stepName("Plus progressive jackpot");
            } else {
//                boolean resPlus = jackpotTrialModeService.plusMultipleJackpots(userId, commandId, progressiveJackpotGroup);
                plusJackpotInfo = jackpotTrialModeService.plusMultipleJackpotsWithAmount(userId, commandId, progressiveJackpotGroup);
                logBuilder.stepName("Plus progressive jackpot for trial mode");
            }
        } else {
            logBuilder.stepName("Plus progressive jackpot").owner(LogMessage.OWNER_JACKPOT).message("Error - Don't add jackpot with value is ZERO")
                .timeExe(Instant.now().toEpochMilli() - lStartTime);
        }
        
        logBuilder.message("Passed - Distributed progressive jackpots "+ GameUtils.convertMapToString(plusJackpotInfo) + " --isTrialMode:" +isTrialMode )
            .timeExe(Instant.now().toEpochMilli() - lStartTime);
        LogsUtils.writeLogDebug(logBuilder.build());
        
        return GameUtils.convertMapToString(plusJackpotInfo);
    }
    
    public static Promotion getPromotion(PromotionRepositoryService promotionRepository, LogMessage.LogMessageBuilder logBuilder,
            String serviceId, String userId, String userType, SpinCmd cmd, String commandId, String currency) {
        long lStartTimeLocal = Instant.now().toEpochMilli();
        Promotion promotion = null;
        promotion = promotionRepository.get(serviceId, userId, currency);
        if(promotion != null ) {
            //log.info("commandId: "+commandId+ " -- UserId:"+userId+" --- betAmount:"+cmd.totalBetId()+" --- Promotion mode run! " + promotion);
            String cmdBetId = cmd.totalBetId();
            long currentTime = Instant.now().toEpochMilli();
            String promotionBetId = promotion.getBetId();
         // promotionBetId;promotionRemain; promotionTotal
            String promotionInfo = promotionBetId + ";" + promotion.getRemain() + ";" + promotion.getTotal();
            
            // check status or expired time
            if (promotion.getExpireAt() < currentTime) { // expired
                promotionRepository.remove(serviceId, userId, currency);
                
                if (SlotGameConstant.PROMOTION_NEW_STATUS.equalsIgnoreCase(promotion.getNotifyStatus())
                        || SlotGameConstant.PROMOTION_RESET_STATUS.equalsIgnoreCase(promotion.getNotifyStatus())) {
                    
                    return null; // ignored promotion when didn't notify
                } else if (SlotGameConstant.PROMOTION_NOTIFIED_STATUS.equalsIgnoreCase(promotion.getNotifyStatus())) {
                    logBuilder.stepName("Check expired ").message("BetId "+ cmdBetId + ". Promotion betId: " + promotionBetId + ". Expired: " + promotion.getExpireAt())
                        .timeExe(Instant.now().toEpochMilli() - lStartTimeLocal);
                    LogsUtils.writeLogError(logBuilder.build());
                    // delete promotion in redis
                    throw new SlotGameException(SlotGameError.PROMOTION_EXPIRE, userId, userType, commandId);
                }
            } else { // not yet expired
                if (SlotGameConstant.PROMOTION_NEW_STATUS.equalsIgnoreCase(promotion.getNotifyStatus())) {
                    // change status
                    promotion.setNotifyStatus(SlotGameConstant.PROMOTION_NOTIFIED_STATUS);
                    promotionRepository.save(serviceId, promotion);
                    throw new SlotGameException(SlotGameError.PROMOTION_NEW, userId, userType, commandId, promotionInfo);
                } else if (SlotGameConstant.PROMOTION_RESET_STATUS.equalsIgnoreCase(promotion.getNotifyStatus())) {
                 // change status
                    promotion.setNotifyStatus(SlotGameConstant.PROMOTION_NOTIFIED_STATUS);
                    promotionRepository.save(serviceId, promotion);
                    throw new SlotGameException(SlotGameError.PROMOTION_RESET, userId, userType, commandId, promotionInfo);
                }
                
                if (!cmdBetId.equals(promotionBetId) && promotion.isValid()) {
                    logBuilder.stepName("Check betId").message("BetId "+ cmdBetId + ". Promotion betId: " + promotionBetId)
                        .timeExe(Instant.now().toEpochMilli() - lStartTimeLocal);
                    LogsUtils.writeLogError(logBuilder.build());
                    throw new SlotGameException(SlotGameError.ERROR_USER_DEFFIRENT_BET_MODE_AWARD, userId, userType, commandId, promotionInfo);
                } 
                
                logBuilder.stepName("Check Promotion mode").message(promotion)
                    .timeExe(Instant.now().toEpochMilli() - lStartTimeLocal);
                LogsUtils.writeLogInfo(logBuilder.build());
            }
        }
        return promotion;
    }
    
    public static BasePlaySession minusWalletWithPromotion(WalletPromotionDto walletProDto) {
        
        long lStartTimeLocal = Instant.now().toEpochMilli();
        BasePlaySessionBuilder builder = walletProDto.basePlaySession().toBuilder();
        if(walletProDto.isTrialMode()) {
            String strResultCode = walletProDto.walletTrialModeService().minusWalletAmount(walletProDto.basePlaySession(), walletProDto.bettingTotal().value().doubleValue(),
                    walletProDto.totalCredit(),
                    walletProDto.serviceCode() + ":"+ walletProDto.serviceName(),
                    walletProDto.prefixService());
            walletProDto.logBuilder().stepName("Minus wallet trial").owner(LogMessage.OWNER_WALLET)
                .message("Minus wallet for trial mode: "+strResultCode +" - isTrialMode: "+ walletProDto.isTrialMode())
                .timeExe(Instant.now().toEpochMilli() - lStartTimeLocal);
            if (!"0".equals(strResultCode)) {
                walletProDto.logBuilder().stepName("Minus wallet Trial Mode").owner(LogMessage.OWNER_GAME)
                    .message("ERROR: Minus wallet  Trial Mode - "+ strResultCode)
                    .timeExe(Instant.now().toEpochMilli() - lStartTimeLocal);
                LogsUtils.writeLogError(walletProDto.logBuilder().build());
                
                if ("2005".equals(strResultCode)) {
                    throw new SlotGameException(SlotGameError.MONEY_NOT_ENOUGH, walletProDto.basePlaySession().userId(), walletProDto.basePlaySession().userType(), walletProDto.basePlaySession().commandId());
                } else {
                    throw new SlotGameException(SlotGameError.UNEXPECTED, walletProDto.basePlaySession().userId(), walletProDto.basePlaySession().userType(), walletProDto.basePlaySession().commandId(),
                        String.format("Error in class %s call to method minusWalletAmount with error code %s", NormalGameServiceImpl.class, strResultCode));
                }
            }
        } else {
              String strResultCode = "";
              if(walletProDto.promotion() != null && walletProDto.promotion().getRemain() > 0 && walletProDto.promotion().isValid()) {
                  walletProDto.logBuilder().stepName("Promotion Spin").owner(LogMessage.OWNER_PROMOTION)
                      .message("Promotion Spin - "+ walletProDto.promotion())
                      .timeExe(Instant.now().toEpochMilli() - lStartTimeLocal);
                  LogsUtils.writeLogDebug(walletProDto.logBuilder().build());
              
//                  promotion = promotionService.minusPromotionData(playSession.serviceId(), playSession.commandId(), playSession.userId(), promotion.getCode(), 
//                      prefixService);

                  walletProDto.promotion(walletProDto.promotionService().reservePromotionData(walletProDto.basePlaySession().serviceId(), walletProDto.basePlaySession().uuid(), 
                      walletProDto.basePlaySession().userId(), walletProDto.promotion().getCode(), walletProDto.prefixService(), walletProDto.userInfo().currency()));
                  
                  if(walletProDto.promotion()  != null) {
                      if (SlotGameConstant.PROMOTION_EXPIRED_CODE == walletProDto.promotion().getStatus()) {
                          walletProDto.promotionRepository().remove(walletProDto.basePlaySession().serviceId(), walletProDto.basePlaySession().userId(), walletProDto.userInfo().currency());
                          throw new SlotGameException(SlotGameError.PROMOTION_EXPIRE, walletProDto.basePlaySession().userId(), walletProDto.basePlaySession().userType(), walletProDto.basePlaySession().commandId());
                      }
                      if(walletProDto.promotion() != null && walletProDto.promotion().isValid()) {
                          builder.promotionCode(walletProDto.promotion().getCode());
                          builder.promotionRemain(walletProDto.promotion().getRemain());
                          builder.promotionTotal(walletProDto.promotion().getTotal());
                          builder.promotionBetId(walletProDto.promotion().getBetId());
                      }
                      if (walletProDto.promotion().getRemain() > 0)
                          walletProDto.promotionRepository().save(walletProDto.basePlaySession().serviceId(), walletProDto.promotion());
                      else {
                          walletProDto.promotionRepository().remove(walletProDto.basePlaySession().serviceId(), walletProDto.basePlaySession().userId(), walletProDto.userInfo().currency());
                      }
                  } else {
                      walletProDto.logBuilder().stepName("Minus wallet service").owner(LogMessage.OWNER_PROMOTION)
                          .message("Error: can't promotion minus! - "+ walletProDto.promotion())
                          .timeExe(Instant.now().toEpochMilli() - lStartTimeLocal);
                      LogsUtils.writeLogError(walletProDto.logBuilder().build());
                  
                      throw new SlotGameException(SlotGameError.UNEXPECTED, walletProDto.basePlaySession().userId(), walletProDto.basePlaySession().userType(), walletProDto.basePlaySession().commandId(),
                          String.format("Error in class %s call to method minusPromotionData return null value.", NormalGameServiceImpl.class));
                  }
                  //call wallet with money = ZERO
                  strResultCode = walletProDto.walletService().minusWalletAmount(builder.build(), Money.ZERO.value().doubleValue(),
                          walletProDto.totalCredit(), walletProDto.serviceCode() + ":"+ walletProDto.serviceName(), walletProDto.prefixService());
                  if (!"0".equals(strResultCode)) {
                      walletProDto.promotion(walletProDto.promotionService().releasePromotionData(walletProDto.basePlaySession().serviceId(), walletProDto.basePlaySession().uuid(), walletProDto.basePlaySession().userId(), walletProDto.promotion().getCode(),
                              walletProDto.prefixService(), walletProDto.userInfo().currency()));
                      switch (strResultCode) {
                          case "2001":
                              throw new SlotGameException(SlotGameError.WALLET_USERID_NULL, walletProDto.basePlaySession().userId(), walletProDto.basePlaySession().userType(), walletProDto.basePlaySession().commandId());
                          case "2004":
                              throw new SlotGameException(SlotGameError.WALLET_MONEY_INVALID, walletProDto.basePlaySession().userId(), walletProDto.basePlaySession().userType(), walletProDto.basePlaySession().commandId());
                          case "2005":
                              throw new SlotGameException(SlotGameError.MONEY_NOT_ENOUGH, walletProDto.basePlaySession().userId(), walletProDto.basePlaySession().userType(), walletProDto.basePlaySession().commandId());
                          case "2003":
                          case "2006":
                              throw new SlotGameException(SlotGameError.WALLET_NOTFOUND_USER, walletProDto.basePlaySession().userId(), walletProDto.basePlaySession().userType(), walletProDto.basePlaySession().commandId());
                          case "2007":
                              throw new SlotGameException(SlotGameError.WALLET_LOCKED_USER, walletProDto.basePlaySession().userId(), walletProDto.basePlaySession().userType(), walletProDto.basePlaySession().commandId());
                          case "2008":
                              throw new SlotGameException(SlotGameError.WALLET_CANNOT_CONNECT, walletProDto.basePlaySession().userId(), walletProDto.basePlaySession().userType(), walletProDto.basePlaySession().commandId());
                          case "2009":
                              throw new SlotGameException(SlotGameError.WALLET_INVALID_ACTION, walletProDto.basePlaySession().userId(), walletProDto.basePlaySession().userType(), walletProDto.basePlaySession().commandId());
                          case "2408":
                              throw new SlotGameException(SlotGameError.WALLET_TIMEOUT, walletProDto.basePlaySession().userId(), walletProDto.basePlaySession().userType(), walletProDto.basePlaySession().commandId());
                          case "2500":
                              throw new SlotGameException(SlotGameError.WALLET_SERVER_ERROR, walletProDto.basePlaySession().userId(), walletProDto.basePlaySession().userType(), walletProDto.basePlaySession().commandId());
                          case "29999":
                              throw new SlotGameException(SlotGameError.WALLET_NO_RESPONSE, walletProDto.basePlaySession().userId(), walletProDto.basePlaySession().userType(), walletProDto.basePlaySession().commandId());
                          default:
                              throw new SlotGameException(SlotGameError.WALLET_UNEXPECTED, walletProDto.basePlaySession().userId(), walletProDto.basePlaySession().userType(), walletProDto.basePlaySession().commandId(),
                                  String.format("Error in class %s call to method minusWalletAmount with error code %s", NormalGameServiceImpl.class, strResultCode));
                      }
                  }
              } else { 
                  if(walletProDto.promotion() != null) {
                      walletProDto.promotionRepository().remove(walletProDto.basePlaySession().serviceId(), walletProDto.basePlaySession().userId(), walletProDto.userInfo().currency());
                  }
                  // subtract player's wallet
                  strResultCode = walletProDto.walletService().minusWalletAmount(builder.build(), walletProDto.bettingTotal().value().doubleValue(),
                          walletProDto.totalCredit(), walletProDto.serviceCode() + ":"+ walletProDto.serviceName(), walletProDto.prefixService());
                  
                  // TODO check Wallet Services API Spec
                  if (!"0".equals(strResultCode)) {
                      walletProDto.logBuilder().stepName("Minus wallet service").owner(LogMessage.OWNER_WALLET)
                          .message("ERROR: Minus wallet - "+ strResultCode)
                          .timeExe(Instant.now().toEpochMilli() - lStartTimeLocal);
                      LogsUtils.writeLogError(walletProDto.logBuilder().build());

                      switch (strResultCode) {
                          case "2001":
                              throw new SlotGameException(SlotGameError.WALLET_USERID_NULL, walletProDto.basePlaySession().userId(), walletProDto.basePlaySession().userType(), walletProDto.basePlaySession().commandId());
                          case "2004":
                              throw new SlotGameException(SlotGameError.WALLET_MONEY_INVALID, walletProDto.basePlaySession().userId(), walletProDto.basePlaySession().userType(), walletProDto.basePlaySession().commandId());
                          case "2005":
                              throw new SlotGameException(SlotGameError.MONEY_NOT_ENOUGH, walletProDto.basePlaySession().userId(), walletProDto.basePlaySession().userType(), walletProDto.basePlaySession().commandId());
                          case "2003":
                          case "2006":
                              throw new SlotGameException(SlotGameError.WALLET_NOTFOUND_USER, walletProDto.basePlaySession().userId(), walletProDto.basePlaySession().userType(), walletProDto.basePlaySession().commandId());
                          case "2007":
                              throw new SlotGameException(SlotGameError.WALLET_LOCKED_USER, walletProDto.basePlaySession().userId(), walletProDto.basePlaySession().userType(), walletProDto.basePlaySession().commandId());
                          case "2008":
                              throw new SlotGameException(SlotGameError.WALLET_CANNOT_CONNECT, walletProDto.basePlaySession().userId(), walletProDto.basePlaySession().userType(), walletProDto.basePlaySession().commandId());
                          case "2009":
                              throw new SlotGameException(SlotGameError.WALLET_INVALID_ACTION, walletProDto.basePlaySession().userId(), walletProDto.basePlaySession().userType(), walletProDto.basePlaySession().commandId());
                          case "2408":
                              throw new SlotGameException(SlotGameError.WALLET_TIMEOUT, walletProDto.basePlaySession().userId(), walletProDto.basePlaySession().userType(), walletProDto.basePlaySession().commandId());
                          case "2500":
                              throw new SlotGameException(SlotGameError.WALLET_SERVER_ERROR, walletProDto.basePlaySession().userId(), walletProDto.basePlaySession().userType(), walletProDto.basePlaySession().commandId());
                          case "29999":
                              throw new SlotGameException(SlotGameError.WALLET_NO_RESPONSE, walletProDto.basePlaySession().userId(), walletProDto.basePlaySession().userType(), walletProDto.basePlaySession().commandId());
                          default:
                              throw new SlotGameException(SlotGameError.WALLET_UNEXPECTED, walletProDto.basePlaySession().userId(), walletProDto.basePlaySession().userType(), walletProDto.basePlaySession().commandId(),
                                  String.format("Error in class %s call to method minusWalletAmount with error code %s", NormalGameServiceImpl.class, strResultCode));
                      }
//                      if ("2005".equals(strResultCode)) {
//                          throw new SlotGameException(SlotGameError.MONEY_NOT_ENOUGH, playSession.userId(), playSession.userType(), playSession.commandId());
//                      } else {
//                          throw new SlotGameException(SlotGameError.UNEXPECTED, playSession.userId(), playSession.userType(), playSession.commandId(),
//                              String.format("Error in class %s call to method minusWalletAmount with error code %s", NormalGameServiceImpl.class, strResultCode));
//                      }
                  }
                  walletProDto.logBuilder().stepName("Minus wallet").owner(LogMessage.OWNER_WALLET)
                      .message("Passed - Minus wallet in normal model"+ strResultCode)
                      .timeExe(Instant.now().toEpochMilli() - lStartTimeLocal);
            }
              
        }
        LogsUtils.writeLogDebug(walletProDto.logBuilder().build());
        return builder.build();
    }

    public static String getPayLineString(BasePlaySession playSession, List<String> payLineList) {
        try {
            return payLineList.stream()
                    .map(pl -> pl.split(";")[0])
                    .collect(Collectors.joining(","));
        } catch (Exception e) {
            LogsUtils.writeLogException(LogMessage.builder()
                    .cmdId(playSession.commandId())
                    .actorId(playSession.userId())
                    .serviceId(playSession.serviceId())
                    .psId(playSession.uuid())
                    .stateName("JackpotLine")
                    .stepName("getPayLineString " + playSession.state())
                    .owner(LogMessage.OWNER_GAME)
                    .build(), e);
            return "";
        }
    }

    public static void initDataForTrialMode(UserService userService, String commandId, UserInfo userInfo,
                                            String serviceId, WalletTrialModeService walletTrialModeService,
                                            JackpotTrialModeService jackpotTrialModeService, double initWalletAmount,
                                            Map<String, Money> jackpotLists) {
        long lStartTimeGolbal = Instant.now().toEpochMilli();
        LogMessage.LogMessageBuilder logBuilder = LogMessage.builder();
        logBuilder
                .cmdId(commandId)
                .actorId(userInfo.userId())
                .serviceId(serviceId)
                .stateName("initDataForTrialMode")
                .owner(LogMessage.OWNER_GAME);

        BasePlaySession playSession = userService.getPlaySession(serviceId, userInfo);
        if (playSession != null && playSession.isTrialMode()) {
            userService.removePlaySession(playSession);
            // removed wallet & jackpot of trial
            walletTrialModeService.removeUser(userInfo.userId());
            jackpotTrialModeService.removeUserJackpot(userInfo.userId(), jackpotLists);
        } else if (playSession != null && !playSession.isTrialMode()) {
            logBuilder.psId(playSession.uuid())
                    .stepName("Validate").owner(LogMessage.OWNER_GAME).message("User can't join trial because we have a normal play session.")
                    .timeExe(0);
            LogsUtils.writeLogError(logBuilder.build());
            throw new SlotGameException(SlotGameError.INVALID_COMMAND, userInfo.userId(), playSession.userType(), commandId);
        }
        if (walletTrialModeService.isTheFirstTimeTrial(userInfo.userId())) {
            walletTrialModeService.initUserAmount(userInfo.userId(), initWalletAmount);
            jackpotTrialModeService.initUserJackpot(userInfo.userId(), jackpotLists);
        }
        logBuilder.stepName("End initDataForTrialMode").message("Init Wallet: " + initWalletAmount
                + ". Init Jackpot: " + jackpotLists)
                .timeExe(Instant.now().toEpochMilli() - lStartTimeGolbal);
        LogsUtils.writeLogDebug(logBuilder.build());
    }
    
    public static String convertMapToString(Map<String, Money> plusJackpotInfo) {
        StringBuilder strBuilder = new StringBuilder();
        if (plusJackpotInfo != null) {
            for (String key : plusJackpotInfo.keySet()) {
                if (strBuilder.length() > 0) {
                    strBuilder.append(",");
                }
                if ((key != null) && (!key.equals(""))) {
                    Money value = plusJackpotInfo.get(key);
                    strBuilder.append(key);
                    strBuilder.append(";");
                    strBuilder.append(value.value().doubleValue());
                }
            }
        }
        return strBuilder.toString();

    }
    
  //For cluster base, clear won normal symbol - ignored WILD in that group
    public static List<DataCell<Symbol>> clearDataWinInRule(List<DataCell<Symbol>> matrixData, List<Integer> traceWay) {
        
        for (int item : traceWay) {
            if (matrixData.get(item).symbol().type() != SymbolType.WILD) {
                matrixData.set(item, null);
            }
        }
        
        return matrixData;
    }
    
    public static DataCell<Symbol>[][] clearDataWinMatrix(DataCell<Symbol>[][] matrixData, List<String> traceWay) {
        for (String item : traceWay) {
            String[] arrItem = item.split(":");
            matrixData[Integer.parseInt(arrItem[0])][Integer.parseInt(arrItem[1])] = null;
        }
        return matrixData;
    }


    public static void updateRtpConfig(String rtp, Environment env, ObjectMapper objectMapper, GameRuleFactory gameRuleFactory, ConfigManager configManager) throws IOException, ClassNotFoundException {
        String title = "Update rtp " + rtp;
        log.info(title);

        String rtpKey = env.getProperty("rtp.key");
        if (StringUtils.isEmpty(rtp) || StringUtils.isEmpty(rtpKey)) {
            return;
        }

        ResourceLoader resourceLoader = new DefaultResourceLoader();
        Gson gson = new Gson();
        String[] keys = rtpKey.split(",");

        for (String key : keys) {
            String clazz_ = env.getProperty(String.format("rtp.clazz.%s", key));
            String file = env.getProperty(String.format("rtp.%s.%s", rtp, key));

            Resource resource = resourceLoader.getResource(file);
            Class<?> clazz = Class.forName(clazz_);
            JsonObject jsonConfig = gson.fromJson(new InputStreamReader(resource.getInputStream()), JsonObject.class);
            ICommonSlotConfig slotConfig = (ICommonSlotConfig) objectMapper.readValue(jsonConfig.toString(), clazz);

            SlotConfigMode mode = SlotConfigMode.getByName(key.toUpperCase());
            slotConfig.initInRuntime(gameRuleFactory);
            configManager.updateMainConfig(mode, slotConfig);

            log.info("{} - Updated bean {} by file {}", title, slotConfig.getClass().getSimpleName(), file);
        }

        log.info("{} - Done.", title);
    }

    public static String formatDouble(double value) {
        return DOUBLE_FORMATTER.format(value);
    }
}
