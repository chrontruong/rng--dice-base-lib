package com.io.begstd.slot.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.io.begstd.slot.model.config.SlotMachineConfigForFree;
import com.io.begstd.slot.model.config.SlotMachineConfigForGamble;
import com.io.begstd.slot.model.config.SlotMachineConfigForMini;
import com.io.begstd.slot.model.config.SlotMachineConfigForNormal;
import com.io.begstd.slot.projection.IPlaySessionProjection;
import com.io.begstd.slot.projection.impl.BasePlaySessionProjectionImpl;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

@Configuration
public class GameConfig {

    @Value("${game.normal.config.file}")
    private Resource normalGameConfig;
    @Value("${game.free.config.file}")
    private Resource freeGameConfig;
    @Value("${game.mini.config.file:classpath:SlotGame_Mini_Config.json}")
    private Resource miniGameConfig;
    @Value("${game.gamble.config.file:classpath:SlotGame_Gamble_Config.json}")
    private Resource gambleGameConfig;

    @Bean
    @SneakyThrows
    public SlotMachineConfigForNormal slotMachineConfigForNormal(ObjectMapper objectMapper) {
        return objectMapper.readValue(normalGameConfig.getInputStream(), new TypeReference<SlotMachineConfigForNormal>() {
        });
    }

    @Bean
    @SneakyThrows
    public SlotMachineConfigForFree slotMachineConfigForFree(ObjectMapper objectMapper) {
        return objectMapper.readValue(freeGameConfig.getInputStream(), new TypeReference<SlotMachineConfigForFree>() {
        });
    }
    
    @Bean
    @SneakyThrows
    @ConditionalOnMissingBean
    public SlotMachineConfigForMini slotMachineConfigForMini(ObjectMapper objectMapper) {
        return objectMapper.readValue(miniGameConfig.getInputStream(), new TypeReference<SlotMachineConfigForMini>() {
        });
    }

    @Bean
    @SneakyThrows
    @ConditionalOnMissingBean
    public SlotMachineConfigForGamble slotMachineConfigForGamble(ObjectMapper objectMapper) {
        return objectMapper.readValue(gambleGameConfig.getInputStream(), new TypeReference<SlotMachineConfigForGamble>() {
        });
    }

    @Bean
    @ConditionalOnMissingBean
    public IPlaySessionProjection playSessionProjection() {
        return new BasePlaySessionProjectionImpl();
    }
}
