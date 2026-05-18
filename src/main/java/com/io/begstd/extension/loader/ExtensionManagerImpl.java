package com.io.begstd.extension.loader;

import com.io.begstd.extension.Extension;
import com.io.begstd.slot.annotation.GameExtension;
import com.io.begstd.slot.exception.ExtensionException;
import com.io.begstd.slot.extension.WonRuleExtensionManager;
import com.io.begstd.slot.factory.GameRuleFactory;
import com.io.begstd.slot.model.config.ISlotMachineConfig;
import com.io.begstd.slot.services.extension.common.*;
import com.io.begstd.slot.services.extension.common.impl.*;
import com.io.begstd.slot.services.extension.gameplay.*;
import com.io.begstd.slot.services.extension.gameplay.impl.*;
import com.io.begstd.slot.services.extension.validator.ValidationExtension;
import com.io.begstd.slot.services.extension.validator.impl.ValidationExtensionImpl;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

@Builder
@Getter
@Setter
@Slf4j
@AllArgsConstructor
@Accessors(fluent = true)
public class ExtensionManagerImpl implements com.io.begstd.extension.manager.ExtensionManager {

    private BettingLinesExtension bettingLinesExtension;
    private DenominationExtension denominationExtension;
    private JackpotExtension jackpotExtension;
    private PlaySessionExtension playSessionExtension;
    private PromotionExtension promotionExtension;
    private SpinListenerExtension spinListenerExtension;
    private SubPlaySessionExtension subPlaySessionExtension;
    private WalletExtension walletExtension;
    private WonRuleExtensionManager wonRuleExtensionManager;
    private ValidationExtension validationExtension;
    private SpinExtension spinExtension;
    private ResumeExtension resumeExtension;

    private BonusGamePlayExtension bonusGamePlayExtension;
    private FreeGameOptionPlayExtension freeGameOptionPlayExtension;
    private LightningGamePlayExtension lightningGamePlayExtension;
    private PowerUpGamePlayExtension powerUpGamePlayExtension;
    private GamblePlayExtension gamblePlayExtension;
    private GambleEndExtension gambleEndExtension;

    private ExtraDataInitGameExtension extraDataInitGameExtension;
    
    public ExtensionManagerImpl() {
        extraDataInitGameExtension =  new ExtraDataInitGameExtensionImpl();
        bettingLinesExtension = new BettingLinesExtenstionImpl();
        denominationExtension = new DenominationExtensionImpl();
        jackpotExtension = new JackpotExtensionImpl();
        promotionExtension = new PromotionExtensionImpl();
        spinListenerExtension = new SpinListenerExtensionImpl();
        subPlaySessionExtension = new SubPlaySessionExtensionImpl();
        walletExtension = new WalletExtensionImpl();
        wonRuleExtensionManager = new WonRuleExtensionManager();
        validationExtension = new ValidationExtensionImpl();
        spinExtension = new SpinExtensionImpl();
        resumeExtension = new ResumeExtensionImpl();
        
        bonusGamePlayExtension = new BonusGamePlayExtensionImpl();
        freeGameOptionPlayExtension = new FreeGameOptionPlayExtensionImpl();
        lightningGamePlayExtension = new LightningGamePlayExtensionImpl();
        powerUpGamePlayExtension = new PowerUpGamePlayExtensionImpl();
        gamblePlayExtension = new GamblePlayExtensionImpl();
        gambleEndExtension = new GambleEndExtensionImpl();

        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(GameExtension.class));
        for (BeanDefinition bd : scanner.findCandidateComponents("com.io.begstd.slot.services.extension")) {
            try {
                Class<?> clazz = Class.forName(bd.getBeanClassName());
                GameExtension ext = clazz.getAnnotation(GameExtension.class);
                registerExtension(ext.extOf(), clazz);
            } catch (ClassNotFoundException | InstantiationException | InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
                log.error(e.getMessage(), e);
            }
        }
    }

    public void verify(List<ISlotMachineConfig> iSlotMachineConfigs, GameRuleFactory gameRuleFactory) {
        wonRuleExtensionManager.verify(iSlotMachineConfigs, gameRuleFactory);
    }

    public ExtensionManagerImpl cloneObject() {
        return ExtensionManagerImpl.builder()
                .extraDataInitGameExtension(extraDataInitGameExtension)
                .bettingLinesExtension(bettingLinesExtension)
                .denominationExtension(denominationExtension)
                .jackpotExtension(jackpotExtension)
                .playSessionExtension(playSessionExtension)
                .promotionExtension(promotionExtension)
                .spinListenerExtension(spinListenerExtension)
                .subPlaySessionExtension(subPlaySessionExtension)
                .walletExtension(walletExtension)
                .wonRuleExtensionManager(wonRuleExtensionManager.clone())
                .validationExtension(validationExtension)
                .bonusGamePlayExtension(bonusGamePlayExtension)
                .freeGameOptionPlayExtension(freeGameOptionPlayExtension)
                .lightningGamePlayExtension(lightningGamePlayExtension)
                .powerUpGamePlayExtension(powerUpGamePlayExtension)
                .spinExtension(spinExtension)
                .resumeExtension(resumeExtension)
                .gamblePlayExtension(gamblePlayExtension)
                .gambleEndExtension(gambleEndExtension)
                .build();

    }

    public void registerExtension(String type, Class clazz) throws NoSuchMethodException, IllegalAccessException,
            InvocationTargetException, InstantiationException {
        switch (type) {
            case "ExtraDataInitGameExtension":
                extraDataInitGameExtension = (ExtraDataInitGameExtension) clazz.getDeclaredConstructor().newInstance();
                break;
            case "BettingLinesExtension":
                bettingLinesExtension = (BettingLinesExtension) clazz.getDeclaredConstructor().newInstance();
                break;
            case "DenominationExtension":
                denominationExtension = (DenominationExtension) clazz.getDeclaredConstructor().newInstance();
                break;
            case "JackpotExtension":
                jackpotExtension = (JackpotExtension) clazz.getDeclaredConstructor().newInstance();
                break;
            case "PlaySessionExtension":
                playSessionExtension = (PlaySessionExtension) clazz.getDeclaredConstructor().newInstance();
                break;
            case "PromotionExtension":
                promotionExtension = (PromotionExtension) clazz.getDeclaredConstructor().newInstance();
                break;
            case "SpinListenerExtension":
                spinListenerExtension = (SpinListenerExtension) clazz.getDeclaredConstructor().newInstance();
                break;
            case "SubPlaySessionExtension":
                subPlaySessionExtension = (SubPlaySessionExtension) clazz.getDeclaredConstructor().newInstance();
                break;
            case "WalletExtension":
                walletExtension = (WalletExtension) clazz.getDeclaredConstructor().newInstance();
                break;
            case "WonRuleExtensionManager":
                wonRuleExtensionManager.addChild((Extension) clazz.getDeclaredConstructor().newInstance());
                break;
            case "ValidationExtension":
                validationExtension = (ValidationExtension) clazz.getDeclaredConstructor().newInstance();
                break;
            case "SpinExtension":
                spinExtension = (SpinExtension) clazz.getDeclaredConstructor().newInstance();
                break;
            case "BonusGamePlayExtension":
                bonusGamePlayExtension = (BonusGamePlayExtension) clazz.getDeclaredConstructor().newInstance();
                break;
            case "FreeGameOptionPlayExtension":
                freeGameOptionPlayExtension = (FreeGameOptionPlayExtension) clazz.getDeclaredConstructor().newInstance();
                break;
            case "LightningGamePlayExtension":
                lightningGamePlayExtension = (LightningGamePlayExtension) clazz.getDeclaredConstructor().newInstance();
                break;
            case "PowerUpGamePlayExtension":
                powerUpGamePlayExtension = (PowerUpGamePlayExtension) clazz.getDeclaredConstructor().newInstance();
                break;
            case "GamblePlayExtension":
                gamblePlayExtension = (GamblePlayExtension) clazz.getDeclaredConstructor().newInstance();
                break;
            case "GambleEndExtension":
                gambleEndExtension = (GambleEndExtension) clazz.getDeclaredConstructor().newInstance();
                break;
            case "ResumeExtension":
                resumeExtension = (ResumeExtension) clazz.getDeclaredConstructor().newInstance();
                break;
                
            default: {
                log.error("No extension match with type {}", type);
                throw new ExtensionException("No extension match with type " + type);
            }
        }
    }

    @Override
    public void verify() {
    }

    @Override
    public <T extends Extension> T getBean(String name) {
        Extension extension;
        switch (name) {
            case "BettingLinesExtension":
                extension = bettingLinesExtension;
                break;
            case "JackpotExtension":
                extension = jackpotExtension;
                break;
            case "PlaySessionExtension":
                extension = playSessionExtension;
                break;
            case "PromotionExtension":
                extension = promotionExtension;
                break;
            case "SpinListenerExtension":
                extension = spinListenerExtension;
                break;
            case "SubPlaySessionExtension":
                extension = subPlaySessionExtension;
                break;
            case "WalletExtension":
                extension = walletExtension;
                break;
            case "WonRuleExtensionManager":
                extension = wonRuleExtensionManager;
                break;
            case "ValidationExtension":
                extension = validationExtension;
                break;
            case "SpinExtension":
                extension = spinExtension;
                break;
            case "BonusGamePlayExtension":
                extension = bonusGamePlayExtension;
                break;
            case "FreeGameOptionPlayExtension":
                extension = freeGameOptionPlayExtension;
                break;
            case "LightningGamePlayExtension":
                extension = lightningGamePlayExtension;
                break;
            case "PowerUpGamePlayExtension":
                extension = powerUpGamePlayExtension;
                break;
            case "GamblePlayExtension":
                extension = gamblePlayExtension;
                break;
            case "GambleEndExtension":
                extension = gambleEndExtension;
                break;
            case "ResumeExtension":
                extension = resumeExtension;
                break;
            default:
                extension = null;
        }
        return (T) extension;
    }

    @Override
    public void putObject(String name, Object o) {
        
    }

    @Override
    public void removeObject(String name, Object o) {
    }

    @Override
    public <T> T getObject(String name) {
        // TODO Auto-generated method stub
        return null;
    }
}
