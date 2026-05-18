package com.io.begstd.slot.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Read configuration
 * 
 */
@Getter
@Setter
@Configuration
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "test")
public class ServiceMockTestConfiguration {

    // TEST MINI GAME
    private boolean miniGame;

    // TEST FREE GAME
    private boolean freeSpin;

    // TEST Jack Pot
    private boolean jackPot;

}
