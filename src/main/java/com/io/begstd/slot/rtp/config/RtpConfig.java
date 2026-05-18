package com.io.begstd.slot.rtp.config;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.io.begstd.slot.rtp.allinone.RtpAllInOneService;
import com.io.begstd.slot.rtp.allinone.RtpAllInOneServiceImpl;
import com.io.begstd.slot.rtp.executor.RtpExecutor;
import com.io.begstd.slot.rtp.repository.MyFileRepository;
import com.io.begstd.slot.rtp.repository.MyFileRepositoryImpl;
import com.io.begstd.slot.rtp.service.RtpService;
import com.io.begstd.slot.rtp.service.internalservice.*;
import com.io.begstd.slot.rtp.task.BaseRtpScenario;
import com.io.begstd.slot.rtp.task.RtpScenario;
import com.io.begstd.slot.services.internal.impl.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;

@Configuration
@Profile("rtp")
public class RtpConfig {

    @Autowired
    RtpProperties rtpProperties;

    @Value("${rtp.config.file}")
    private Resource rtpConfigFile;

    @Bean("customizeExecutor")
    @ConditionalOnMissingBean
    public TaskExecutor getAsyncExecutor() {
        ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
        threadPoolTaskExecutor.setCorePoolSize(rtpProperties.getCorePoolSize());
        threadPoolTaskExecutor.setMaxPoolSize(rtpProperties.getMaxPoolSize());
        threadPoolTaskExecutor.setWaitForTasksToCompleteOnShutdown(false);
        threadPoolTaskExecutor.setThreadNamePrefix("RTP-");
        threadPoolTaskExecutor.initialize();
        return threadPoolTaskExecutor;
    }

    @Bean
    @ConditionalOnMissingBean
    RtpScenario rtpScenario() {
        return new BaseRtpScenario();
    }

    @Bean
    @ConditionalOnMissingBean
    RtpExecutor rtpExecutor() {
        return new RtpExecutor();
    }

    @Bean
    @ConditionalOnMissingBean
    RtpService rtpService() {
        return new BaseRtpService();
    }

    @Bean
    @ConditionalOnMissingBean
    RtpNormalGameService rtpNormalGameService() {
        return new BaseRtpNormalGameService();
    }

    @Bean
    @ConditionalOnMissingBean
    RtpMiniGameService rtpMiniGameService() {
        return new BaseRtpMiniGameService();
    }

    @Bean
    @ConditionalOnMissingBean
    RtpFreeGameService rtpFreeGameService() {
        return new BaseRtpFreeGameService();
    }

    @Bean
    @ConditionalOnMissingBean
    RtpFreeSpinOptionGameService rtpFreeSpinOptionGameService() {
        return new BaseRtpFreeSpinOptionGameService();
    }
    
    @Bean
    @ConditionalOnMissingBean
    MyFileRepository myFileRepository() {
        return new MyFileRepositoryImpl();
    }
    
    @Bean
    @ConditionalOnMissingBean
    RtpPowerUpGameService rtpPowerUpGameService() {
        return new BaseRtpPowerUpGameService();
    }
    
    @Bean
    @ConditionalOnMissingBean
    RtpLightningGameService rtpLightningGameService() {
        return new BaseRtpLightningGameService();
    }

    @Bean
    @ConditionalOnMissingBean
    public RtpGambleGameService rtpGambleGameService() {
        return new BaseRtpGambleGameService();
    }


    @Bean
    @ConditionalOnMissingBean
    public NormalGameServiceImpl normalGameServiceImpl() {
        return new NormalGameServiceImpl();
    }

    @Bean
    @ConditionalOnMissingBean
    public FreeGameServiceImpl freeGameServiceImpl() {
        return new FreeGameServiceImpl();
    }

    @Bean
    @ConditionalOnMissingBean
    public FreeSpinOptionGameServiceImpl freeSpinOptionGameServiceImpl() {
        return new FreeSpinOptionGameServiceImpl();
    }

    @Bean
    @ConditionalOnMissingBean
    public MiniGameServiceImpl miniGameServiceImpl() {
        return new MiniGameServiceImpl();
    }

    @Bean
    @ConditionalOnMissingBean
    public LightningGameServiceImpl lightningGameServiceImpl() {
        return new LightningGameServiceImpl();
    }

    @Bean
    @ConditionalOnMissingBean
    public PowerUpGameServiceImpl powerUpGameServiceImpl() {
        return new PowerUpGameServiceImpl();
    }

    @Bean
    public RtpAllInOneService rtpAllInOneService() {
        return new RtpAllInOneServiceImpl();
    }

    @Bean
    public Map<String, ?> rtpAllInOneConfig() throws IOException {
        return new Gson().fromJson(new JsonReader(
                new InputStreamReader(rtpConfigFile.getInputStream(), UTF_8)),
                Map.class);
    }

}
