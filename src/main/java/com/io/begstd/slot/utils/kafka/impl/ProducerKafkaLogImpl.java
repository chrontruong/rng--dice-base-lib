package com.io.begstd.slot.utils.kafka.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.io.begstd.slot.common.SlotGameConstant;
import com.io.begstd.slot.model.app.BasePlaySession;
import com.io.begstd.slot.model.app.GameState;
import com.io.begstd.slot.model.app.JackpotInfo;
import com.io.begstd.slot.model.config.SlotMachineConfigForFree;
import com.io.begstd.slot.model.config.SlotMachineConfigForNormal;
import com.io.begstd.slot.model.domain.Money;
import com.io.begstd.slot.model.gamerule.BettingLine;
import com.io.begstd.slot.model.gamerule.Symbol;
import com.io.begstd.slot.model.playsession.FreeGameProb;
import com.io.begstd.slot.utils.BeanUtils;
import com.io.begstd.slot.utils.GameUtils;
import com.io.begstd.slot.utils.JsonParseUtils;
import com.io.begstd.slot.utils.MatrixUtil;
import com.io.begstd.slot.utils.kafka.IExtraData;
import com.io.begstd.slot.utils.kafka.IProducerKafkaLog;
import com.io.begstd.slot.utils.kafka.impl.JackpotInfoKafka.JackpotInfoKafkaBuilder;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class ProducerKafkaLogImpl implements IProducerKafkaLog{
    
    public String getKafkaMessage(BasePlaySession basePlaySession, String prefixService, String messageWallet) {
        //store log to kafka
        String userType = "";
        if (SlotGameConstant.NBOT_TYPE.equals(basePlaySession.userType())) {
            userType = SlotGameConstant.BOT_TYPE;
        } else {
            userType = basePlaySession.userType();
        }

        KafkaMessage kafkaMess = new KafkaMessage();
        if (basePlaySession.isTrialMode()) {
            kafkaMess.logType("TrialGResult");
        } else {
            kafkaMess.logType("GameResult");
        }
        kafkaMess.category("slot");
        kafkaMess.userId(basePlaySession.userId());
        kafkaMess.userType(userType);
        kafkaMess.displayName(basePlaySession.displayName() == null ? "": basePlaySession.displayName());
        kafkaMess.avatar(basePlaySession.avatar() == null ? "" : basePlaySession.avatar());
        kafkaMess.userIP(basePlaySession.ip() == null ? "" : basePlaySession.ip());
        kafkaMess.env(String.valueOf(basePlaySession.env() > 0 ? basePlaySession.env():"N/A"));
        kafkaMess.ssid(StringUtils.isEmpty(basePlaySession.ssid()) ? "N/A" : basePlaySession.ssid());
        kafkaMess.playSessionId(basePlaySession.uuid());
        kafkaMess.requestId(basePlaySession.commandId());
        kafkaMess.requestType("Command");
        kafkaMess.service(prefixService + basePlaySession.serviceId());
        kafkaMess.internal(String.valueOf(basePlaySession.serviceId()));
        kafkaMess.action(getGameState(basePlaySession.state()));
        LogInfo logInfo = new LogInfo();
//        logInfo.payLoad(new PayLoad(playSession.userId(), userType));
        logInfo.result(clonePlaySession(basePlaySession, messageWallet));
        kafkaMess.logInfo(logInfo);
        kafkaMess.timeUTC(Instant.now().toEpochMilli());
        try {
            return JsonParseUtils.parseToJson(kafkaMess);
        } catch (JsonProcessingException e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }
    
    public PlaySessionToKafka clonePlaySession(BasePlaySession ps, String messageWallet) {
        PlaySessionToKafka result = initPlaySessionKafka();
        result = clonePlaySessionCommon(ps, result, messageWallet);
        result = cloneExtendSession(ps, result);
        return result;
    }

    protected PlaySessionToKafka clonePlaySessionCommon(BasePlaySession ps, PlaySessionToKafka result, String messageWallet) {
          SlotMachineConfigForNormal configNormal = BeanUtils.getBean(SlotMachineConfigForNormal.class);
          SlotMachineConfigForFree configFree = BeanUtils.getBean(SlotMachineConfigForFree.class);
          
          if (ps != null) {
              result.playSessionId(ps.uuid()); 
              result.commandId(ps.commandId());
              result.state(getGameState(ps.state()));
              result.betId(ps.betDenom().id());
              result.currency(ps.currency());
              result.lang(Strings.isBlank(ps.lang()) ? "VI" : ps.lang());
              result.betDenom(ps.betDenom().amount().value().floatValue());
              result.betAmt(ps.totalBet().value().doubleValue());
              result.jpInfoAmt(ps.jpInfoAmt() != null ? ps.jpInfoAmt() : "");
              if (ps.bettingLines() != null) {
                  result.bettingLines(buildBettingLine(ps.bettingLines()));
              }
              
              if (ps.latestWinAmount() != null && ps.latestWinAmount().isGreaterThan(Money.ZERO)) {
                  result.lastWinAmount(ps.latestWinAmount().value().doubleValue());
              } 
              
//              if (ps.latestWinBonusAmount() != null && ps.latestWinBonusAmount().isGreaterThan(Money.ZERO)) {
//                      result.lastWinAmount(ps.latestWinBonusAmount().value().doubleValue());
//              }
              if (ps.freeSpinProb() != null) {
                  FreeGameProb freeProb = (FreeGameProb) ps.freeSpinProb();
                  result.selectedOption(freeProb.freeSpinWonId());
              }

              result.totalWinAmount(ps.winAmount().value().doubleValue());
              result.normalWinAmount(ps.normalGameWinAmount().value().doubleValue());
              if (ps.normalGameWinAmount().isGreaterThan(Money.ZERO)) { 
                  if (ps.normalGamePayLines() != null && ps.normalGamePayLines().size() > 0) {
                      //betperline  
                      StringBuffer resultTemmp = new StringBuffer();    
                      if (ps.bettingLines() != null && ps.bettingLines().size() > 0) {  
                            
                          for (String line : ps.normalGamePayLines()) { 
                              String[] arrLine = line.split(";");   
                               resultTemmp.append("L" + arrLine[0] + "("+ arrLine[1] +"."+ arrLine[3] +") + "); 
                          } 
                      } else {  
                          //allwaytowin S3;120.00;reel 3;com 1;12   
                          for (String line : ps.normalGamePayLines()) { 
                              String[] arrLine = line.split(";");   
                              resultTemmp.append(arrLine[2] +"."+ arrLine[0] +"x"+arrLine[3] + " + ");  
                          } 
                      } 
                      result.paylineNormal(resultTemmp.toString().substring(0, resultTemmp.toString().length()-2)); 
                  } 
              }
              
              switch (ps.state()) {
                  case GameState.NORMAL_GAME:
                      if (ps.normalGamePayLines() != null) {
                          result.latestPaylines(ps.normalGamePayLines().toString());
                      }
                      break;
                  case GameState.FREE_GAME:
                      if (ps.freeGamePayLines() != null) {
                          result.latestPaylines(ps.freeGamePayLines().toString());
                      }
                      break;
              }
//              result.jackpotWinAmount(ps.winJackpotAmount().value().doubleValue());
//              result.jackpotId(ps.jackpotId());
              if (ps.normalGameMatrix() != null) {
                  List<String> normalMatrix = MatrixUtil.convertToMatrixCode1DByReel(ps.normalGameMatrix(), ps.normalGameTableFormat());
                  result.normalMatrixCode(normalMatrix);
                  result.normalGameTableFormat(ps.normalGameTableFormat());
                  result.normalMatrixName(getSymbolNameMatrix(configNormal.getSymbolsForTest(normalMatrix.toArray(String[]::new))));
              }
              
              if ((ps.freeGameMatrix() != null)) {
                  List<String> freeMatrix = MatrixUtil.convertToMatrixCode1DByReel(ps.freeGameMatrix(), ps.freeGameTableFormat());
                  result.freeMatrixCode(freeMatrix);
                  result.freeGameTableFormat(ps.freeGameTableFormat());
                  result.freeMatrixName(getSymbolNameMatrix(configFree.getSymbolsForTest(freeMatrix.toArray(String[]::new))));
              }
              result.freeGameTotal(ps.freeGameTotal());
              result.freeGameRemain(ps.freeGameRemain());
              result.freeGameWinAmount(ps.freeGameWinAmount().value().doubleValue());
              
              if (ps.freeSpinProb() != null) {
                  result.multiplier(((FreeGameProb)ps.freeSpinProb()).wonMultiplier());
                  result.wonWildCount(((FreeGameProb)ps.freeSpinProb()).wonCount());
                  result.selectedOption(((FreeGameProb)ps.freeSpinProb()).freeSpinWonId());
              }
              
              result.bonusGameTotal(ps.bonusGameTotal());
              result.bonusGameRemain(ps.bonusGameRemain());
              result.bonusGamePlayRemain(ps.bonusPlayRemain());
              result.bonusGameWinAmount(ps.bonusGameWinAmount().value().doubleValue());
              result.jackpotWinAmount(ps.winJackpotAmount() != null ? ps.winJackpotAmount().value().doubleValue() : Money.ZERO.value().doubleValue());

              if(!CollectionUtils.isEmpty(ps.jackpotHistory())) {
                  List<JackpotInfoKafka> jpKafkas = new ArrayList<>();
                  for(JackpotInfo jpInfo : ps.jackpotHistory()) {
                      JackpotInfoKafkaBuilder jpBuilder = JackpotInfoKafka.builder();
                      jpBuilder
                              .jackpotAmount(jpInfo.jackpotAmount() == null ? 0.0 : jpInfo.jackpotAmount().value().doubleValue())
                              .jackpotId(jpInfo.jackpotId())
                              .winTime(jpInfo.winTime())
                              .userId(jpInfo.userId())
                              .state(getGameState(jpInfo.state()))
                              .level(jpInfo.level())
                              .extraData(cloneExtraJackpotInfo(ps, jpInfo));
                      jpKafkas.add(jpBuilder.build());
                  }
                  result.jackpotInfo(jpKafkas);
              }
              //build latestJackpotInfo
              if (ps.latestWinJackpotAmount().isGreaterThan(Money.ZERO)) {
                  JackpotInfo lastJPHistory = ps.jackpotHistory().get(ps.jackpotHistory().size()-1);
                  JackpotInfoKafkaBuilder jpBuilder = JackpotInfoKafka.builder();
                  jpBuilder
                          .jackpotAmount(lastJPHistory.jackpotAmount() == null ? 0.0 : lastJPHistory.jackpotAmount().value().doubleValue())
                          .jackpotId(lastJPHistory.jackpotId())
                          .userId(lastJPHistory.userId())
                          .winTime(lastJPHistory.winTime())
                          .level(lastJPHistory.level())
                          .state(getGameState(lastJPHistory.state()))
                          .extraData(cloneExtraLatestJackpotInfo(ps, lastJPHistory));

                  if (result.latestJackpotInfo() == null) {
                      result.latestJackpotInfo(new ArrayList<>());
                  }
                  result.latestJackpotInfo().add(jpBuilder.build());

                  if (ps.lastJackpotInfo() != null) {
                      result.lastJPLine(ps.lastJackpotInfo().toString());
                  }
              }
              
              result = cloneBonusMatrix(ps, result);
              result.isFinish(ps.isFinished());
              result.messageWallet(messageWallet);
              
              if(!StringUtils.isEmpty(ps.promotionCode())) {
                  result.messageWallet("Bonus");
                  result.promotionRemain(ps.promotionRemain());
                  result.promotionTotal(ps.promotionTotal());
                  result.promotionCode(ps.promotionCode());
                  result.promotionBetId(ps.promotionBetId());
              }
/*
              if (ps.quest() != null) {
                  QuestLog questLog = QuestLog.builder()
                          .eventId(ps.quest().getEventId())
                          .questId(ps.quest().getQuestId())
                          .awardQuestMoney(ps.quest().getAwardQuestMoney())
                          .awardEventMoney(ps.quest().getAwardEventMoney())
                          .isComplete(ps.quest().isComplete())
                          .endTime(ps.quest().getEndTime())
                          .isLastQuest(ps.quest().isLastQuest())
                          .winQuestMoney(ps.questAmount() != null ? ps.questAmount().value().doubleValue() : 0.0)
                          .winEventMoney(ps.eventAmount() != null ? ps.eventAmount().value().doubleValue() : 0.0)
                          .taskInfo(getTaskInfoString(ps.quest()))
                          .build();

                  result.quest(questLog);
              }
*/
              result.wo(ps.walletOption());
          }
          return result;
      }
      
     protected PlaySessionToKafka cloneRespinMatrix(BasePlaySession ps, PlaySessionToKafka result) {
        log.error("Game need to implement adding respin matrix to kafka");
        return result;
      }
    
      protected PlaySessionToKafka cloneBonusMatrix(BasePlaySession ps, PlaySessionToKafka result1) {
          if (ps.bonusGameMatrix() != null) {
              List<String> bonusMatrix = GameUtils.convertBonusMatrixIDToList(ps.bonusGameMatrix(), ps.bonusGameTableFormat());
              List<String> bonusGameMatrixName = GameUtils.convertBonusMatrixCodeToList(ps.bonusGameMatrix(), 
                  ps.bonusGameTableFormat());
              //              List<String> bonusGameMatrix = new ArrayList<>();
//              for(DataCell<ISymbolBonusGame> dataCell : ps.bonusGameMatrix()[0]) {
//                  if (dataCell != null && dataCell.symbol() != null) {
//                      SymbolBonusGame reSymbol = (SymbolBonusGame) dataCell.symbol();
//                      bonusGameMatrix.add(String.valueOf(reSymbol.id()));
//                  }
//              }
              result1.bonusMatrixCode(bonusMatrix);
              result1.bonusMatrixName(bonusGameMatrixName);
              result1.bonusGameTableFormat(ps.bonusGameTableFormat());
          }
          return result1;
      }
      
      protected PlaySessionToKafka cloneExtendSession(BasePlaySession ps, PlaySessionToKafka result) {
          return result;
      }

      protected IExtraData cloneExtraLatestJackpotInfo(BasePlaySession ps, JackpotInfo jackpotInfo) {
          return null;
      }

      protected IExtraData cloneExtraJackpotInfo(BasePlaySession ps, JackpotInfo jackpotInfo) {
          return null;
      }
      
      protected List<String> getSymbolNameMatrix(Symbol[] matrixSymbol){
          List<String> resultMatrix = new ArrayList<String>();
          for (int i = 0; i< matrixSymbol.length; i++) {
              resultMatrix.add(matrixSymbol[i].name());
          }
          return resultMatrix;
      }
      
      protected String getGameState(int gameState) {
          
         switch (gameState) {
             case 1:
                 return "NormalGame";
             case 2:
                 return "FreeGame";
             case 3: 
                 return "FreeGameOption";
             case 5:
                 return "LightningGame";
             case 6:
                 return "PowerUpGame";
             case 7:
                 return "GambleGame";
             default:
                 return "BonusGame";
         }
      }
      
      private String buildBettingLine(List<BettingLine> bettingLines) {
          if(CollectionUtils.isEmpty(bettingLines)) {
              return null;
          }
          List<Integer> result = new ArrayList<Integer>();
          for (BettingLine item : bettingLines) {
              if (item != null) { 
                  result.add(item.line().id());
              }
          }
          return result.toString();
      }
/*
    private String getTaskInfoString(Quest quest) {
        if (quest.getTasks() == null) {
            return null;
        }
        return quest.getTasks().stream()
                .map(task ->
                        task.getId() + ";" +
                        task.getName() + ";" +
                        GameUtils.formatDouble(task.getScore()) + "/" + GameUtils.formatDouble(task.getTargetScore()))
                .collect(Collectors.joining(","));
    }
    */
}
