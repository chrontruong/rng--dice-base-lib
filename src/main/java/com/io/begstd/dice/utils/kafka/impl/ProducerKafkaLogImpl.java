package com.io.begstd.dice.utils.kafka.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.io.begstd.dice.model.app.BasePlaySession;
import com.io.begstd.dice.model.app.JackpotInfo;
import com.io.begstd.dice.model.domain.Money;
import com.io.begstd.dice.utils.JsonParseUtils;
import com.io.begstd.dice.utils.kafka.IExtraData;
import com.io.begstd.dice.utils.kafka.IProducerKafkaLog;
import com.io.begstd.dice.common.DiceGameConstant;
import com.io.begstd.dice.utils.kafka.impl.JackpotInfoKafka.JackpotInfoKafkaBuilder;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class ProducerKafkaLogImpl implements IProducerKafkaLog {

    public String getKafkaMessage(BasePlaySession basePlaySession, String prefixService, String messageWallet) {
        //store log to kafka
        String userType = "";
        if (DiceGameConstant.NBOT_TYPE.equals(basePlaySession.userType())) {
            userType = DiceGameConstant.BOT_TYPE;
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
        kafkaMess.displayName(basePlaySession.displayName() == null ? "" : basePlaySession.displayName());
        kafkaMess.avatar(basePlaySession.avatar() == null ? "" : basePlaySession.avatar());
        kafkaMess.userIP(basePlaySession.ip() == null ? "" : basePlaySession.ip());
        kafkaMess.env(String.valueOf(basePlaySession.env() > 0 ? basePlaySession.env() : "N/A"));
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

            if (ps.latestWinAmount() != null && ps.latestWinAmount().isGreaterThan(Money.ZERO)) {
                result.lastWinAmount(ps.latestWinAmount().value().doubleValue());
            }

            result.totalWinAmount(ps.winAmount().value().doubleValue());
            result.normalWinAmount(ps.normalGameWinAmount().value().doubleValue());
            result.jackpotWinAmount(ps.winJackpotAmount() != null ? ps.winJackpotAmount().value().doubleValue() : Money.ZERO.value().doubleValue());

            if (!CollectionUtils.isEmpty(ps.jackpotHistory())) {
                List<JackpotInfoKafka> jpKafkas = new ArrayList<>();
                for (JackpotInfo jpInfo : ps.jackpotHistory()) {
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
                JackpotInfo lastJPHistory = ps.jackpotHistory().get(ps.jackpotHistory().size() - 1);
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

            result.isFinish(ps.isFinished());
            result.messageWallet(messageWallet);

            if (!StringUtils.isEmpty(ps.promotionCode())) {
                result.messageWallet("Bonus");
                result.promotionRemain(ps.promotionRemain());
                result.promotionTotal(ps.promotionTotal());
                result.promotionCode(ps.promotionCode());
                result.promotionBetId(ps.promotionBetId());
            }
            result.wo(ps.walletOption());
        }
        return result;
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

    protected String getGameState(int gameState) {

        switch (gameState) {
            case 1:
                return "NormalGame";
            default:
                return null;
        }
    }
}
