package com.io.begstd.dice.config;

import com.io.begstd.dice.config.model.RedisKeyListener;
import com.io.begstd.dice.model.app.Promotion;
import com.io.begstd.dice.model.config.IDiceMachineConfig;
import com.io.begstd.dice.model.config.DiceConfigMode;
import com.io.begstd.dice.repository.IRedisDiceConfigRepository;
import com.io.begstd.dice.repository.PromotionRepositoryService;
import com.io.begstd.dice.repository.RedisCommandIdHistoryRepository;
import com.io.begstd.dice.repository.RedisConfigRTPRepository;
import com.io.begstd.dice.repository.impl.InRedisCommandIdHistoryRepositoryImpl;
import com.io.begstd.dice.repository.impl.PromotionRepositoryServiceImpl;
import com.io.begstd.dice.repository.impl.PromotionRepositoryServiceImplTest;
import com.io.begstd.dice.repository.impl.RedisCommandIdHistoryTestRepository;
import com.io.begstd.dice.repository.impl.RedisConfigRTPRepositoryImpl;
import com.io.begstd.dice.repository.impl.RedisConfigRTPRepositoryTestImpl;
import com.io.begstd.dice.repository.impl.RedisDiceConfigRepositoryImpl;
import com.io.begstd.dice.utils.ConfigManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisClientConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

import static com.io.begstd.dice.config.model.RedisKeyListener.REDIS_KEY_SPACE;

/**
 * Redis configuration
 */
@Configuration
@EnableConfigurationProperties
@EnableRedisRepositories(basePackages = "com.io.begstd.dice.repository")
@Slf4j
public class RedisConfiguration {

    @Value("${redis.hostname}")
    private String redisHostName;

    @Value("${redis.port}")
    private int redisPort;

    @Value("${redis.index:0}")
    private int index;

    @Value("${redis2.ip2}")
    private String redisHostName2;

    @Value("${redis2.port2}")
    private int redisPort2;


    @Value("${jedis.readTimeout:30}")
    private int readTimeout;
    @Value("${jedis.connectTimeout:60}")
    private int connectTimeout;
    @Value("${jedis.pool.maxIdle:30}")
    private int maxIdle;
    @Value("${jedis.pool.minIdle:10}")
    private int minIdle;
    @Value("${jedis.pool.maxTotal:120}")
    private int maxTotal;

    @Bean
    @Primary
    public JedisConnectionFactory jedisConnectionFactory() {
        return createJedisConnectionFactory(redisHostName, redisPort, index);
    }

    @Bean
    public RedisTemplate<String, String> redisTemplate() {
        final RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(jedisConnectionFactory());

        return template;
    }

    @Bean
    public RedisTemplate<String, String> redisTemplateNoHashKey() {
        final RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(jedisConnectionFactory());

        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new StringRedisSerializer());

        // the following is not required
        template.setHashValueSerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());

        return template;
    }

    @Bean
    public JedisConnectionFactory jedisConnectionFactoryRTP() {
        return createJedisConnectionFactory(redisHostName2, redisPort2, 0);
    }

    private JedisConnectionFactory createJedisConnectionFactory(String host, int port, int index) {
        RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration();
        configuration.setHostName(host);
        configuration.setPort(port);
        configuration.setDatabase(index);

        JedisClientConfiguration clientConfiguration
                = JedisClientConfiguration.builder()
                .readTimeout(Duration.ofSeconds(readTimeout))
                .connectTimeout(Duration.ofSeconds(connectTimeout))
                .usePooling()
                .build();

        JedisConnectionFactory jedisConFactory = new JedisConnectionFactory(configuration, clientConfiguration);
        jedisConFactory.getPoolConfig().setMaxIdle(maxIdle);
        jedisConFactory.getPoolConfig().setMinIdle(minIdle);
        jedisConFactory.getPoolConfig().setMaxTotal(maxTotal);

        return jedisConFactory;
    }

    @Bean
    public RedisTemplate<String, String> redisTemplateRTP() {
        final RedisTemplate<String, String> templateRTP = new RedisTemplate<>();
        templateRTP.setConnectionFactory(jedisConnectionFactoryRTP());
        templateRTP.setKeySerializer(new StringRedisSerializer());
        templateRTP.setValueSerializer(new StringRedisSerializer());

        // the following is not required
        templateRTP.setHashValueSerializer(new StringRedisSerializer());
        templateRTP.setHashKeySerializer(new StringRedisSerializer());
        return templateRTP;
    }

    @Bean
    public RedisTemplate<String, Promotion> redisPromotionGameTemplate() {
        final RedisTemplate<String, Promotion> template = new RedisTemplate<>();
        template.setConnectionFactory(jedisConnectionFactory());
        return template;
    }

    @Bean
    @ConditionalOnProperty(
            value = "service.externalServiceType.promotionService",
            havingValue = "prod",
            matchIfMissing = true
    )
    public PromotionRepositoryService promotionRepositoryService() {
        return new PromotionRepositoryServiceImpl(redisPromotionGameTemplate());
    }

    @Bean
    @ConditionalOnProperty(
            value = "service.externalServiceType.promotionService",
            havingValue = "test",
            matchIfMissing = false
    )
    public PromotionRepositoryService promotionRepositoryServiceTest() {
        return new PromotionRepositoryServiceImplTest();
    }

    @Bean
    @ConditionalOnProperty(
            value = "service.internalServiceType.commandIdHistory",
            havingValue = "prod",
            matchIfMissing = true
    )
    public RedisCommandIdHistoryRepository redisCommandIdHistoryRepository() {
        return new InRedisCommandIdHistoryRepositoryImpl(redisTemplate());
    }

    @Bean
    @ConditionalOnProperty(
            value = "service.internalServiceType.commandIdHistory",
            havingValue = "test",
            matchIfMissing = false
    )
    public RedisCommandIdHistoryRepository redisCommandIdHistoryTestRepository() {
        return new RedisCommandIdHistoryTestRepository();
    }

    @Bean
    @ConditionalOnProperty(
            value = "service.internalServiceType.rtpConfigService",
            havingValue = "prod",
            matchIfMissing = true
    )
    public RedisConfigRTPRepository redisConfigRTPRepositoryImpl() {
        return new RedisConfigRTPRepositoryImpl(redisTemplateRTP());
    }

    @Bean
    @ConditionalOnProperty(
            value = "service.internalServiceType.rtpConfigService",
            havingValue = "test",
            matchIfMissing = false
    )
    public RedisConfigRTPRepository redisConfigRTPRepositoryTestImpl() {
        return new RedisConfigRTPRepositoryTestImpl();
    }

    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(
            @Qualifier("jedisConnectionFactoryRTP") JedisConnectionFactory connectionFactory,
            RedisKeyListener redisKeyListener,
            @Value("${redis2.gameConfigKeyPatterns}") String[] patterns,
            ConfigManager configManager) {
        IDiceMachineConfig slotMachineConfigForNormal = (IDiceMachineConfig) configManager.getConfigMain(DiceConfigMode.NORMAL);
        RedisMessageListenerContainer listenerContainer = new RedisMessageListenerContainer();
        listenerContainer.setConnectionFactory(connectionFactory);
        if (patterns != null) {
            for (String pattern : patterns) {
                listenerContainer.addMessageListener(redisKeyListener,
                        new PatternTopic(REDIS_KEY_SPACE + pattern + "_" + slotMachineConfigForNormal.serviceId()));
            }
        }
        listenerContainer.setErrorHandler(
                e -> {
                    log.error("Error in redisMessageListenerContainer", e);
                    System.exit(1);
                });
        return listenerContainer;
    }

    @Bean
    @ConditionalOnMissingBean
    public RedisKeyListener redisKeyListener() {
        return new RedisKeyListener();
    }

    @Bean
    public IRedisDiceConfigRepository redisDiceConfigRepository() {
        return new RedisDiceConfigRepositoryImpl(redisTemplateRTP());
    }
}
