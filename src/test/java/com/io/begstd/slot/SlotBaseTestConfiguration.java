package com.io.begstd.slot;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.util.ISO8601DateFormat;
import com.io.begstd.extension.config.ExtensionLoaderBeanConfig;
import com.io.begstd.slot.config.ExternalServiceEndPointConfiguration;
import com.io.begstd.slot.config.GameConfig;
import com.io.begstd.slot.config.KafkaConfig;
import com.io.begstd.slot.config.MatrixGenerateConfiguration;
import com.io.begstd.slot.config.StartTestProperty;
import com.io.begstd.slot.game.test.MatrixDataForTest;
import com.io.begstd.slot.machine.ISlotGameMachine;
import com.io.begstd.slot.machine.impl.BaseSlotMachineImpl;
import com.io.begstd.slot.model.app.BasePlaySession;
import com.io.begstd.slot.model.app.CommandIdHistory;
import com.io.begstd.slot.model.app.UserInfo;
import com.io.begstd.slot.projection.IPlaySessionProjection;
import com.io.begstd.slot.repository.IRedisSlotConfigRepository;
import com.io.begstd.slot.repository.PromotionRepositoryService;
import com.io.begstd.slot.repository.RedisConfigRTPRepository;
import com.io.begstd.slot.repository.impl.PromotionRepositoryServiceImplTest;
import com.io.begstd.slot.repository.impl.RedisConfigRTPRepositoryTestImpl;
import com.io.begstd.slot.repository.impl.RedisSlotConfigRepositoryImpl;
import com.io.begstd.slot.services.external.IPlayerViewStoreClient;
import com.io.begstd.slot.services.external.JackpotService;
import com.io.begstd.slot.services.external.PlayerViewStoreService;
import com.io.begstd.slot.services.external.PromotionService;
import com.io.begstd.slot.services.external.impl.JackpotServiceTestImpl;
import com.io.begstd.slot.services.external.impl.PlayerViewStoreClientTestImpl;
import com.io.begstd.slot.services.external.impl.PlayerViewStoreServiceImpl;
import com.io.begstd.slot.services.external.impl.PromotionServiceTestImpl;
import com.io.begstd.slot.services.internal.BaseTrialService;
import com.io.begstd.slot.services.internal.FreeGameService;
import com.io.begstd.slot.services.internal.FreeSpinOptionGameService;
import com.io.begstd.slot.services.internal.GambleService;
import com.io.begstd.slot.services.internal.JackpotTrialModeService;
import com.io.begstd.slot.services.internal.LightningGameService;
import com.io.begstd.slot.services.internal.MiniGameService;
import com.io.begstd.slot.services.internal.NormalGameService;
import com.io.begstd.slot.services.internal.PowerUpGameService;
import com.io.begstd.slot.services.internal.RespinGameService;
import com.io.begstd.slot.services.internal.SlotGameService;
import com.io.begstd.slot.services.internal.StartTestSchedulerService;
import com.io.begstd.slot.services.internal.UserLockService;
import com.io.begstd.slot.services.internal.UserService;
import com.io.begstd.slot.services.internal.WalletTrialModeService;
import com.io.begstd.slot.services.internal.impl.FreeGameServiceImpl;
import com.io.begstd.slot.services.internal.impl.FreeSpinOptionGameServiceImpl;
import com.io.begstd.slot.services.internal.impl.GambleServiceImpl;
import com.io.begstd.slot.services.internal.impl.JackpotServiceTrialModeTestImpl;
import com.io.begstd.slot.services.internal.impl.LightningGameServiceImpl;
import com.io.begstd.slot.services.internal.impl.MiniGameServiceImpl;
import com.io.begstd.slot.services.internal.impl.NormalGameServiceImpl;
import com.io.begstd.slot.services.internal.impl.PowerUpGameServiceImpl;
import com.io.begstd.slot.services.internal.impl.QueueHistoryServiceImpl;
import com.io.begstd.slot.services.internal.impl.RespinGameServiceImpl;
import com.io.begstd.slot.services.internal.impl.SlotGameServiceImpl;
import com.io.begstd.slot.services.internal.impl.SlotGameTrialService;
import com.io.begstd.slot.services.internal.impl.StartTestSchedulerServiceTestImpl;
import com.io.begstd.slot.services.internal.impl.UserLockServiceImpl;
import com.io.begstd.slot.services.internal.impl.WalletServiceTrialModeTestImpl;
import com.io.begstd.slot.utils.BeanUtils;
import com.io.begstd.slot.utils.ConfigManager;
import com.io.begstd.wallet.service.WalletService;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@TestConfiguration
@Import({
        GameConfig.class, MatrixGenerateConfiguration.class,
        KafkaConfig.class, ExtensionLoaderBeanConfig.class})
@PropertySource("classpath:application.properties")
@ComponentScan({"com.io.begstd.slot.machine.wonrule.*",
        "com.io.begstd.slot.factory",
        "com.io.begstd.slot.rules.matrix.generate.reel.impl",
        "com.io.begstd.slot.rules.matrix.transform.impl",
        "com.io.begstd.slot.extension"
})
public class SlotBaseTestConfiguration {

    @PostConstruct
    public void init() {

    }

    @Bean
    UserService userService(SessionStorge sessionStorge) {
        UserService userService = mock(UserService.class);
        //    	doReturn(true).when(userService).isJoinedGame(anyString(), anyString());
        when(userService.isJoinedGame(anyString(), any(UserInfo.class))).thenAnswer((Answer<Boolean>) invocation -> {
            return true;
        });

        when(userService.getPlaySession(anyString(), any(UserInfo.class))).then((Answer<BasePlaySession>) invocation -> {
            UserInfo userInfo = invocation.getArgument(1);
            return sessionStorge.get(userInfo.userId());
        });
        doAnswer((Answer<Void>) invocation -> {
            BasePlaySession basePlaySession = invocation.getArgument(0);
            sessionStorge.put(basePlaySession.userId(), basePlaySession);
            return null;
        }).when(userService).savePlaySession(any(BasePlaySession.class));

        doAnswer((Answer<Void>) invocation -> {
            BasePlaySession basePlaySession = invocation.getArgument(0);
            if (basePlaySession != null && basePlaySession.userId() != null) {
                sessionStorge.remove(basePlaySession.userId());
            }
            return null;
        }).when(userService).removePlaySession(any(BasePlaySession.class));

        return userService;
    }

    @Bean
    public WalletService walletService() {
        WalletService walletService = mock(WalletService.class);

        when(walletService.addWalletAmount(any(BasePlaySession.class), anyInt(), anyString(), anyString()))
                .then((Answer<String>) invocation -> {
                    BasePlaySession ps = invocation.getArgument(0);
                    if ("user_add_wallet_error".equals(ps.userId())) {
                        return "1000";
                    }
                    return "0";
                });

        when(walletService.minusWalletAmount(any(BasePlaySession.class), anyDouble(), anyInt(), anyString(), anyString()))
                .then((Answer<String>) invocation -> {
                    BasePlaySession ps = invocation.getArgument(0);
                    if ("user_not_enough_money".equalsIgnoreCase(ps.userId())) {
                        return "2005";
                    }
                    if ("user_unexpect".equals(ps.userId())) {
                        return "10000";
                    }
                    return "0";
                });
        return walletService;
    }

    @Bean
    public ExternalServiceEndPointConfiguration externalServiceEndPointConfiguration() {
        return new ExternalServiceEndPointConfiguration();
    }

    @Bean
    public IPlayerViewStoreClient playerViewStoreClient() {
        return new PlayerViewStoreClientTestImpl();
    }

    @Bean
    PlayerViewStoreService playerViewStoreService(IPlaySessionProjection projection) {
        PlayerViewStoreService playerViewStoreService = new PlayerViewStoreServiceImpl();
        PlayerViewStoreService spyBean = spy(playerViewStoreService);
        doAnswer((Answer<Boolean>) invocation -> {
            BasePlaySession basePlaySession = invocation.getArgument(0);
            Object viewerObj = projection.convertToFullViewModel(basePlaySession);
            return true;
        }).when(spyBean).updateState(any(BasePlaySession.class));
//        when(spyBean.updateState(any(PlaySession.class))).then(
//                (Answer<Boolean>) invocation  ->{
//                    PlaySession playSession = invocation.getArgument(0);
//                    Object viewerObj = projection.convertToViewModel(playSession);
//                    return true;
//        });
        return spyBean;
    }

    ;

    @Bean
    public BeanUtils beanUtils() {
        return new BeanUtils();
    }

    @Bean
    public UserLockService userLockService() {
        return new UserLockServiceImpl();
    }

    @Bean("objectMapper")
    ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        objectMapper.setVisibility(PropertyAccessor.SETTER, JsonAutoDetect.Visibility.NONE);
        objectMapper.setVisibility(PropertyAccessor.GETTER, JsonAutoDetect.Visibility.NONE);
        objectMapper.setVisibility(PropertyAccessor.IS_GETTER, JsonAutoDetect.Visibility.NONE);
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        objectMapper.setDateFormat(new ISO8601DateFormat());

        return objectMapper;
    }

    @Bean("objectSerialize")
    ObjectMapper objectSerialize() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.enableDefaultTypingAsProperty(ObjectMapper.DefaultTyping.NON_CONCRETE_AND_ARRAYS, "_class");
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        objectMapper.setVisibility(PropertyAccessor.SETTER, JsonAutoDetect.Visibility.NONE);
        objectMapper.setVisibility(PropertyAccessor.GETTER, JsonAutoDetect.Visibility.NONE);
        objectMapper.setVisibility(PropertyAccessor.IS_GETTER, JsonAutoDetect.Visibility.NONE);
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        objectMapper.setDateFormat(new ISO8601DateFormat());

        return objectMapper;
    }

    @Bean
    public JackpotService jackpotServiceForTest() {
        return new JackpotServiceTestImpl();
    }


    @Bean
    public PromotionService promotionServiceForTest() {
        return new PromotionServiceTestImpl();
    }

    @Bean
    public PromotionRepositoryService promotionRepositoryServiceTest() {
        return new PromotionRepositoryServiceImplTest();
    }

    @Bean
    SessionStorge sessionStorge() {
        return new SessionStorge();
    }

    public class SessionStorge {
        private Map<String, BasePlaySession> maps = new HashMap<>();

        public void put(String userId, BasePlaySession basePlaySession) {
            maps.put(userId, basePlaySession);
        }

        public BasePlaySession get(String userId) {
            return maps.get(userId);
        }

        public BasePlaySession remove(String userId) {
            return maps.remove(userId);
        }

        public void clean() {
            maps.clear();
        }
    }

    @Bean
    RedisMatrixMock redisMatrixMock() {
        return new RedisMatrixMock();
    }

    public class RedisMatrixMock {
        private Map<String, MatrixDataForTest> maps = new HashMap<>();

        public void put(String userId, MatrixDataForTest playSession) {
            maps.put(userId, playSession);
        }

        public MatrixDataForTest get(String userId) {
            return maps.get(userId);
        }

        public MatrixDataForTest remove(String userId) {
            return maps.remove(userId);
        }

        public void clean() {
            maps.clear();
        }
    }


    @Bean("mockJp")
    Map<String, String> mockJp() {
        return new HashMap<>();
    }

    @Bean("mockBonus")
    Map<String, String> mockBonus() {
        return new HashMap<>();
    }

    @Bean
    public NormalGameService normalGameService() {
        return new NormalGameServiceImpl();
    }

    @Bean
    public MiniGameService miniGameService() {
        return new MiniGameServiceImpl();
    }

    @Bean
    public FreeGameService freeGameService() {
        return new FreeGameServiceImpl();
    }
    
    @Bean
    public RespinGameService respinGameService() {
        return new RespinGameServiceImpl();
    }

    @Bean
    FreeSpinOptionGameService freeSpinOptionGameService() {
        return new FreeSpinOptionGameServiceImpl();
    }

    @Bean
    public SlotGameService slotGameService() {
        return new SlotGameServiceImpl();
    }

    @Bean
    public JedisConnectionFactory jedisConnectionFactory() {
        JedisConnectionFactory jedisConFactory = new JedisConnectionFactory();
        jedisConFactory.setHostName("localhost");
        jedisConFactory.setPort(6379);
        jedisConFactory.setUsePool(true);
        return jedisConFactory;
    }

    @Bean
    public RedisTemplate<String, String> redisTemplate() {
        final RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(jedisConnectionFactory());
        return template;
    }

    @Bean
    public GambleService gambleService() {
        return new GambleServiceImpl();
    }

    @Bean
    public LightningGameService lightningGameService() {
        return new LightningGameServiceImpl();
    }

    @Bean
    public PowerUpGameService powerUpGameService() {
        return new PowerUpGameServiceImpl();
    }

    @Bean
    public WalletTrialModeService walletTrialModeServiceTest() {
        return new WalletServiceTrialModeTestImpl();
    }

    @Bean
    public JackpotTrialModeService JackpotTrialModeServiceTest() {
        return new JackpotServiceTrialModeTestImpl();
    }

    @Bean
    public RedisConfigRTPRepository redisConfigRTPRepositoryTestImpl() {
        return new RedisConfigRTPRepositoryTestImpl();
    }

    @Bean
    public StartTestProperty startTestProperty() {
        return new StartTestProperty();
    }

    @Bean
    public QueueHistoryServiceImpl<String, CommandIdHistory> queueHistoryUtils() {
        return new QueueHistoryServiceImpl<String, CommandIdHistory>();
    }

    @Bean
    public ISlotGameMachine slotGameMachine() {
        return new BaseSlotMachineImpl();
    }

    @Bean
    public ConfigManager configManager() {
        return new ConfigManager();
    }

    @Bean
    public IRedisSlotConfigRepository redisSlotConfigRepository() {
        return new RedisSlotConfigRepositoryImpl(redisTemplate());
    }

    @Bean
    public StartTestSchedulerService startTestScheduler() {
        return new StartTestSchedulerServiceTestImpl();
    }

    @Bean
    public BaseTrialService trialService() {
        return new SlotGameTrialService();
    }
}
