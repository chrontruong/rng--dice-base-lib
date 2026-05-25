package com.io.begstd.dice.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.io.begstd.dice.model.config.DiceMachineConfigForNormal;
import com.io.begstd.dice.projection.IPlaySessionProjection;
import com.io.begstd.dice.projection.impl.BasePlaySessionProjectionImpl;
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

    @Bean
    @SneakyThrows
    public DiceMachineConfigForNormal slotMachineConfigForNormal(ObjectMapper objectMapper) {
        return objectMapper.readValue(normalGameConfig.getInputStream(), new TypeReference<DiceMachineConfigForNormal>() {
        });
    }

    @Bean
    @ConditionalOnMissingBean
    public IPlaySessionProjection playSessionProjection() {
        return new BasePlaySessionProjectionImpl();
    }
}
