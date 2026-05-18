package com.io.begstd.slot.config;

import com.io.begstd.slot.config.model.WalletMappingConfig;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Read configuration from 'service' prefix
 * 
 */
@Getter
@Setter
@Configuration
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "service")
public class ExternalServiceEndPointConfiguration {

    private String jackpotHostName;
    private int jackpotHostPort;
    
//    private String walletHostName;
//    private int walletHostPort;
    private List<WalletMappingConfig> walletMapping;

    private String playerViewStoreHostName;
    private int playerViewStoreHostPort;
    
    private String promotionHostName;
    private int promotionHostPort;
    
    private int maxRetrySending;
    
    private boolean enableKafka;
    
    private boolean startTest;

    private long pingKafkaInterval;


    
}
