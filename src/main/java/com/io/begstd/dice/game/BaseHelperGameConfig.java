package com.io.begstd.dice.game;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.io.begstd.dice.model.config.IDiceMachineConfig;
import com.io.begstd.dice.model.config.DiceConfigMode;
import com.io.begstd.dice.model.domain.Money;
import com.io.begstd.dice.model.gamerule.DenominationLevel;
import com.io.begstd.dice.utils.ConfigManager;
import com.io.begstd.dice.command.DenominationLevelCmd;
import com.io.begstd.dice.command.ExtraBetLevelCmd;
import com.io.begstd.dice.config.StartTestProperty;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class BaseHelperGameConfig implements IHelperGameConfig {

    @Autowired
    private StartTestProperty startTestProperty;

    @Autowired
    private ConfigManager configManager;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public ObjectNode getServiceGameConfig() {
        IDiceMachineConfig normalConfig = (IDiceMachineConfig) configManager.getConfigMain(DiceConfigMode.NORMAL);
        ObjectNode node = objectMapper.createObjectNode();
        node.put("serviceId", normalConfig.serviceId());
        node.put("exServiceId", normalConfig.prefixService() + normalConfig.serviceId());
        node.put("serviceName", normalConfig.serviceName());
        node.put("serviceType", "slot");
        node.set("description", null);
        if (startTestProperty.getRtp() == null || startTestProperty.getRtp().isEmpty()) {
            node.put("rtp", Strings.EMPTY);
        } else {
            node.put("rtp", Double.parseDouble(startTestProperty.getRtp()) / normalConfig.totalPercent());
        }

        ArrayNode betLevels = objectMapper.createArrayNode();
        ArrayNode jackpots = objectMapper.createArrayNode();

        Map<String, List<String>> jpIdBetIdMap = new HashMap<>();
        Map<String, String> jpIdCurrencyMap = new HashMap<>();
        Map<String, List<String>> betIdMap = new HashMap<>();

        List<DenominationLevelCmd> denominations = normalConfig.denominationLevels()
                .stream()
                .filter(denom -> CollectionUtils.isNotEmpty(denom.env()))
                .collect(Collectors.toList());
        for (DenominationLevelCmd denom : denominations) {
            if (normalConfig.extraBetLevels() != null && normalConfig.extraBetLevels().size() > 0) {
                for (ExtraBetLevelCmd extbet : normalConfig.extraBetLevels()) {
                    ObjectNode betLevel = objectMapper.createObjectNode();
                    betLevel.put("betId", denom.id().charAt(0) + extbet.id());
                    betLevel.put("betDenom", denom.amount());
                    betLevel.put("hasPromotion", denom.env().contains(1));
                    betLevel.put("betAmount", Money.of(denom.amount())
                            .multiply(normalConfig.totalCredit())
                            .multiply(extbet.amount()).value()
                            .doubleValue());
                    betLevel.put("betCurrency", denom.curr());
                    String betId = denom.id() + "_" + denom.curr();
                    if (!betIdMap.containsKey(betId)) {
                        betIdMap.put(betId, new ArrayList<>());
                    }
                    betIdMap.get(betId).add(betLevel.get("betId").asText());
                    betLevels.add(betLevel);
                }
            } else {
                ObjectNode betLevel = objectMapper.createObjectNode();
                betLevel.put("betId", denom.id());
                betLevel.put("betDenom", denom.amount());
                betLevel.put("hasPromotion", denom.env().contains(1));
                betLevel.put("betAmount", Money.of(denom.amount())
                        .multiply(normalConfig.totalCredit())
                        .value().doubleValue());
                betLevel.put("betCurrency", denom.curr());
                betLevels.add(betLevel);

                String betId = denom.id() + "_" + denom.curr();
                if (!betIdMap.containsKey(betId)) {
                    betIdMap.put(betId, new ArrayList<>());
                }
                betIdMap.get(betId).add(betLevel.get("betId").asText());
            }
        }

        for (DenominationLevel denom : normalConfig.getDenominationLevels()) {
            if (!jpIdBetIdMap.containsKey(denom.jackpotID())) {
                jpIdBetIdMap.put(denom.jackpotID(), new ArrayList<>());
                jpIdCurrencyMap.put(denom.jackpotID(), denom.curr());
            }
            jpIdBetIdMap.get(denom.jackpotID()).addAll(betIdMap.get(denom.id() + "_" + denom.curr()));
        }

        normalConfig.JACKPOTS().forEach((id, amt) -> {
            String jpType = id.substring(id.lastIndexOf("_") + 1);
            if (jpIdBetIdMap.containsKey(id)) {
                for (String betId : jpIdBetIdMap.get(id)) {
                    ObjectNode jackpot = objectMapper.createObjectNode();
                    jackpot.put("jpId", id);
                    jackpot.put("jpType", jpType);
                    jackpot.put("betId", betId);
                    jackpot.put("initAmount", amt.value().doubleValue());
                    jackpot.put("currency", jpIdCurrencyMap.get(id));
                    jackpots.add(jackpot);
                }
            }
        });

        node.set("betLevel", betLevels);
        node.set("jackpot", jackpots);
        return node;
    }
}

