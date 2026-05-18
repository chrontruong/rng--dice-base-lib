package com.io.begstd.slot.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.util.ISO8601DateFormat;
import com.io.begstd.extension.config.ExtensionLoaderBeanConfig;
import com.io.begstd.log.config.LogBeanConfig;
import com.io.begstd.slot.game.BaseHelperGameConfig;
import com.io.begstd.slot.game.IHelperGameConfig;
import com.io.begstd.slot.machine.ISlotGameMachine;
import com.io.begstd.slot.machine.impl.BaseSlotMachineImpl;
import com.io.begstd.slot.model.app.BasePlaySession;
import com.io.begstd.slot.model.app.IPlaySessionClass;
import com.io.begstd.slot.model.config.ISlotMachineConfig;
import com.io.begstd.slot.model.gamerule.Symbol;
import com.io.begstd.slot.model.playsession.ISymbol;
import com.io.begstd.slot.model.playsession.ISymbolBonusGame;
import com.io.begstd.slot.qc.QCToolService;
import com.io.begstd.slot.services.external.IPlayerViewStoreClient;
import com.io.begstd.slot.services.external.JackpotService;
import com.io.begstd.slot.services.external.PlayerViewStoreService;
import com.io.begstd.slot.services.external.PromotionService;
import com.io.begstd.slot.services.external.impl.*;
import com.io.begstd.slot.services.internal.*;
import com.io.begstd.slot.services.internal.impl.*;
import com.io.begstd.slot.utils.ConfigManager;
import com.io.begstd.slot.utils.CustomizeDeserialize;
import com.io.begstd.wallet.config.WalletBeanConfig;
import com.io.begstd.wallet.service.WalletPlaySessionConverter;
import com.io.begstd.wallet.service.WalletService;
import com.io.begstd.wallet.service.impl.WalletServiceImpl;
import com.io.begstd.wallet.service.impl.WalletServiceTestImpl;
import org.lognet.springboot.grpc.GRpcServerBuilderConfigurer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.context.AnnotationConfigServletWebServerApplicationContext;
import org.springframework.context.ApplicationContext;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.*;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Configuration
@Import({
        ExtensionLoaderBeanConfig.class,
        WalletBeanConfig.class,
        LogBeanConfig.class
})
public class BeanConfig<K, V> {
    @Bean
    public MessageSource messageSource() {
        ReloadableResourceBundleMessageSource messageSource
                = new ReloadableResourceBundleMessageSource();

        messageSource.setBasename("classpath:locale/messages");
        messageSource.setDefaultEncoding("UTF-8");
        return messageSource;
    }

    @Bean
    public MessageSourceAccessor messageSourceAccessor() {
        return new MessageSourceAccessor(messageSource());
    }

    @Bean("objectMapper")
    @Primary
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
    //@ConditionalOnMissingBean
    ObjectMapper objectSerialize(List<CustomizeDeserialize> customizeDeserializes) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        objectMapper.setVisibility(PropertyAccessor.SETTER, JsonAutoDetect.Visibility.NONE);
        objectMapper.setVisibility(PropertyAccessor.GETTER, JsonAutoDetect.Visibility.NONE);
        objectMapper.setVisibility(PropertyAccessor.IS_GETTER, JsonAutoDetect.Visibility.NONE);
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_DEFAULT);
        objectMapper.setDateFormat(new ISO8601DateFormat());
        SimpleModule module =
                new SimpleModule("DeserializerModule",
                        new Version(1, 0, 0, null));

        List<CustomizeDeserialize> iBonusDeserializers = new ArrayList<>();
        List<CustomizeDeserialize> iSymbolDeserializers = new ArrayList<>();
        List<CustomizeDeserialize> symbolDeserializers = new ArrayList<>();

        for (CustomizeDeserialize customizeDeserialize : customizeDeserializes) {
            if (customizeDeserialize.getClassType().equals(ISymbolBonusGame.class)) {
                iBonusDeserializers.add(customizeDeserialize);
            } else if (customizeDeserialize.getClassType().equals(ISymbol.class)) {
                iSymbolDeserializers.add(customizeDeserialize);
            } else if (customizeDeserialize.getClassType().equals(Symbol.class)) {
                symbolDeserializers.add(customizeDeserialize);
            } else {
                module.addDeserializer(customizeDeserialize.getClassType(), (JsonDeserializer) customizeDeserialize);
            }
        }

        for (CustomizeDeserialize customizeDeserialize :  iSymbolDeserializers) {
            module.addDeserializer(customizeDeserialize.getClassType(), (JsonDeserializer) customizeDeserialize);
        }
        for (CustomizeDeserialize customizeDeserialize :  symbolDeserializers) {
            module.addDeserializer(customizeDeserialize.getClassType(), (JsonDeserializer) customizeDeserialize);
        }
        for (CustomizeDeserialize customizeDeserialize :  iBonusDeserializers) {
            module.addDeserializer(customizeDeserialize.getClassType(), (JsonDeserializer) customizeDeserialize);
        }

        objectMapper.registerModule(module);



        return objectMapper;
    }

    //Defining beans for internal services

    @Bean
    @ConditionalOnMissingBean
    public SlotGameService slotGameService() {
        return new SlotGameServiceImpl();
    }

    @Bean
    @ConditionalOnMissingBean
    public NormalGameService normalGameService() {
        return new NormalGameServiceImpl();
    }

    @Bean
    @ConditionalOnMissingBean
    public FreeGameService freeGameService() {
        return new FreeGameServiceImpl();
    }
    
    @Bean
    @ConditionalOnMissingBean
    public RespinGameService respinGameService() {
        return new RespinGameServiceImpl();
    }

    @Bean
    @ConditionalOnMissingBean
    public FreeSpinOptionGameService freeSpinOptionGameService() {
        return new FreeSpinOptionGameServiceImpl();
    }

    @Bean
    @ConditionalOnMissingBean
    public MiniGameService miniGameService() {
        return new MiniGameServiceImpl();
    }

    @Bean
    @ConditionalOnMissingBean
    public GambleService gambleService() {
        return new GambleServiceImpl();
    }

    @Bean
    @ConditionalOnMissingBean
    public LightningGameService lightningGameService() {
        return new LightningGameServiceImpl();
    }

    @Bean
    @ConditionalOnMissingBean
    public PowerUpGameService powerUpGameService() {
        return new PowerUpGameServiceImpl();
    }

    @Bean
    @ConditionalOnMissingBean
    public UserService userService() {
        return new UserServiceImpl();
    }


    //Defining beans for external services
    @Bean
    @ConditionalOnProperty(
            value = "service.externalServiceType.jackpotService",
            havingValue = "prod",
            matchIfMissing = true
    )
    public JackpotService jackpotService() {
        return new JackpotServiceImpl();
    }

    @Bean
    @ConditionalOnProperty(
            value = "service.externalServiceType.jackpotService",
            havingValue = "test",
            matchIfMissing = false
    )
    public JackpotService jackpotServiceForTest() {
        return new JackpotServiceTestImpl();
    }

    @Bean
    @ConditionalOnProperty(
            value = "service.externalServiceType.jackpotService",
            havingValue = "disable",
            matchIfMissing = false
    )
    public JackpotService jackpotServiceDisable() {
        return new JackpotServiceDisableImpl();
    }

    @Bean
    @ConditionalOnProperty(
            value = "service.externalServiceType.walletService",
            havingValue = "prod",
            matchIfMissing = true
    )
    public WalletService walletService() {
        return new WalletServiceImpl();
    }

    @Bean
    @ConditionalOnProperty(
            value = "service.externalServiceType.walletService",
            havingValue = "test",
            matchIfMissing = false
    )
    public WalletService walletServiceForTest() {
        return new WalletServiceTestImpl();
    }

    @Bean
    @ConditionalOnMissingBean
    public PlayerViewStoreService playerViewStoreService() {
        return new PlayerViewStoreServiceImpl();
    }

    @Bean
    @ConditionalOnProperty(
            value = "service.externalServiceType.playViewStoreService",
            havingValue = "prod",
            matchIfMissing = true
    )
    public IPlayerViewStoreClient playerViewStoreClient() {
        return new PlayerViewStoreClientImpl();
    }

    @Bean
    @ConditionalOnProperty(
            value = "service.externalServiceType.playViewStoreService",
            havingValue = "test",
            matchIfMissing = false
    )
    public IPlayerViewStoreClient playerViewStoreClientTest() {
        return new PlayerViewStoreClientTestImpl();
    }

    @Bean
    @ConditionalOnProperty(
            value = "service.externalServiceType.promotionService",
            havingValue = "prod",
            matchIfMissing = true
    )
    public PromotionService promotionService() {
        return new PromotionServiceImpl();
    }

    @Bean
    @ConditionalOnProperty(
            value = "service.externalServiceType.promotionService",
            havingValue = "test",
            matchIfMissing = false
    )
    public PromotionService promotionServiceForTest() {
        return new PromotionServiceTestImpl();
    }

    @Bean
    @ConditionalOnProperty(
            value = "service.internalServiceType.jackpotTrialModeService",
            havingValue = "prod",
            matchIfMissing = true
    )
    public JackpotTrialModeService jackpotTrialModeService() {
        return new JackpotServiceTrialModeImpl();
    }

    @Bean
    @ConditionalOnProperty(
            value = "service.internalServiceType.jackpotTrialModeService",
            havingValue = "test",
            matchIfMissing = false
    )
    public JackpotTrialModeService JackpotTrialModeServiceTest() {
        return new JackpotServiceTrialModeTestImpl();
    }

    @Bean
    @ConditionalOnProperty(
            value = "service.internalServiceType.walletTrialModeService",
            havingValue = "prod",
            matchIfMissing = true
    )
    public WalletTrialModeService walletTrialModeService() {
        return new WalletServiceTrialModeImpl();
    }

    @Bean
    @ConditionalOnProperty(
            value = "service.internalServiceType.walletTrialModeService",
            havingValue = "test",
            matchIfMissing = false
    )
    public WalletTrialModeService walletTrialModeServiceTest() {
        return new WalletServiceTrialModeTestImpl();
    }

    @Bean
    @ConditionalOnMissingBean
    public StartTestProperty startTestProperty() {
        return new StartTestProperty();
    }

    @Bean
    public IPlaySessionClass playSessionClass() {
        return () -> BasePlaySession.class;
    }

    @Bean
    public ConfigManager configManager() {
        return new ConfigManager();
    }

    @Bean
    @ConditionalOnProperty(
            value = "service.internalServiceType.activePlayerService",
            havingValue = "prod",
            matchIfMissing = true
    )
    public ActivePlayerService activePlayerService() {
        return new ActivePlayerServiceImpl();
    }

    @Bean
    @ConditionalOnProperty(
            value = "service.internalServiceType.activePlayerService",
            havingValue = "test",
            matchIfMissing = false
    )
    public ActivePlayerService activePlayerServiceTest() {
        return new ActivePlayerServiceTestImpl();
    }

    @Bean
    @ConditionalOnMissingBean
    public ISlotGameMachine slotGameMachine() {
        return new BaseSlotMachineImpl();
    }

    @Bean
    public WalletPlaySessionConverter walletPlaySessionConverter() {
        return new WalletPlaySessionConverterImpl();
    }

    @Bean
    public GRpcServerBuilderConfigurer gRpcServerBuilderConfigurer() {
        return new CustomizeGRpcServerBuilderConfigurer();
    }

    @Bean
    @Profile("!rtp")
    public StartTestSchedulerService startTestSchedulerService() {
        return new StartTestSchedulerServiceImpl();
    }

    @Bean
    public IHelperGameConfig helperGameConfig() {
        return new BaseHelperGameConfig();
    }

    @Bean
    public BaseTrialService trialService() {
        return new SlotGameTrialService();
    }

    @Bean
    @ConditionalOnMissingBean
    public IRandomService randomService() {
        return new BaseRandomService();
    }

}
