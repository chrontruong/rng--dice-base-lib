package com.io.begstd.slot.config;

import com.io.begstd.slot.utils.kafka.IProducerKafkaLog;
import com.io.begstd.slot.utils.kafka.impl.ProducerKafkaLogImpl;
import com.io.begstd.slot.utils.kafka.impl.ProducerKafkaLogTestImpl;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaConfig {
    @Bean
    @ConditionalOnMissingBean(name = "kafkaLogForTest")
    @ConditionalOnProperty(
            value = "spring.kafka.enviromentType",
            havingValue = "test",
            matchIfMissing = false
    )
    public IProducerKafkaLog kafkaLogForTest() {
        return new ProducerKafkaLogTestImpl();
    }
    
    @Bean
    @ConditionalOnMissingBean(name = "kafkaLogService")
    @ConditionalOnProperty(
            value = "spring.kafka.enviromentType",
            havingValue = "prod",
            matchIfMissing = true
    )
    public IProducerKafkaLog kafkaLogService() {
        return new ProducerKafkaLogImpl();
    }
}
