package com.io.begstd.dice.rtp.config;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.io.begstd.dice.rtp.allinone.RtpAllInOneService;
import com.io.begstd.dice.rtp.allinone.RtpAllInOneServiceImpl;
import com.io.begstd.dice.rtp.executor.RtpExecutor;
import com.io.begstd.dice.rtp.repository.MyFileRepository;
import com.io.begstd.dice.rtp.repository.MyFileRepositoryImpl;
import com.io.begstd.dice.rtp.service.RtpService;
import com.io.begstd.dice.rtp.service.internalservice.BaseRtpNormalGameService;
import com.io.begstd.dice.rtp.service.internalservice.BaseRtpService;
import com.io.begstd.dice.rtp.service.internalservice.RtpNormalGameService;
import com.io.begstd.dice.rtp.task.BaseRtpScenario;
import com.io.begstd.dice.rtp.task.RtpScenario;
import com.io.begstd.dice.services.internal.impl.NormalGameServiceImpl;
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
    MyFileRepository myFileRepository() {
        return new MyFileRepositoryImpl();
    }

    @Bean
    @ConditionalOnMissingBean
    public NormalGameServiceImpl normalGameServiceImpl() {
        return new NormalGameServiceImpl();
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
