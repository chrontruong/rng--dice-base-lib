package com.io.begstd.dice.utils.kafka;

import com.io.begstd.dice.config.ExternalServiceEndPointConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

import java.time.Instant;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import static java.util.concurrent.TimeUnit.MINUTES;

@Configuration
@EnableScheduling
@Slf4j
public class KafkaHealCheckServiceImpl implements SchedulingConfigurer {

    @Autowired
    private ExternalServiceEndPointConfiguration externalServiceConfig;

    private static long lastUpdateAt;
    private ScheduledExecutorService scheduler;

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskScheduler) {
        ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadPoolTaskScheduler();
        threadPoolTaskScheduler.setPoolSize(1);
        threadPoolTaskScheduler.setThreadNamePrefix("ping-kafka-scheduler");
        threadPoolTaskScheduler.initialize();
        taskScheduler.setTaskScheduler(threadPoolTaskScheduler);

        scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleWithFixedDelay(this::ping, 0, 1, MINUTES);
    }

    void ping(){
        log.info("isEnableKafka {}", externalServiceConfig.isEnableKafka());
        if (externalServiceConfig.isEnableKafka()) {
            // Log first message to fetch meta data from kafka server
            long delta = Instant.now().toEpochMilli() - lastUpdateAt;
            if(delta > externalServiceConfig.getPingKafkaInterval() * 1000){
                log.info("{\"message\":\"Ping Kafka - GameResult\", \"userType\":\"USER\"}");
                log.info("{\"message\":\"Ping Kafka - GameResult\", \"userType\":\"BOT\"}");
                updateTime();
            }
        }
    }

    public static void updateTime(){
        lastUpdateAt = Instant.now().toEpochMilli();
    }

}
