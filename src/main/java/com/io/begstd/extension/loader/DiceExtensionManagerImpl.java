package com.io.begstd.extension.loader;

import com.io.begstd.extension.Extension;
import com.io.begstd.dice.annotation.GameExtension;
import com.io.begstd.dice.exception.ExtensionException;
import com.io.begstd.dice.extension.WonRuleExtensionManager;
import com.io.begstd.dice.factory.GameRuleFactory;
import com.io.begstd.dice.model.config.IDiceMachineConfig;
import com.io.begstd.dice.services.extension.common.DenominationExtension;
import com.io.begstd.dice.services.extension.common.ExtraDataInitGameExtension;
import com.io.begstd.dice.services.extension.common.JackpotExtension;
import com.io.begstd.dice.services.extension.common.PlaySessionExtension;
import com.io.begstd.dice.services.extension.common.PromotionExtension;
import com.io.begstd.dice.services.extension.common.ResumeExtension;
import com.io.begstd.dice.services.extension.common.SpinExtension;
import com.io.begstd.dice.services.extension.common.SpinListenerExtension;
import com.io.begstd.dice.services.extension.common.SubPlaySessionExtension;
import com.io.begstd.dice.services.extension.common.WalletExtension;
import com.io.begstd.dice.services.extension.common.impl.DenominationExtensionImpl;
import com.io.begstd.dice.services.extension.common.impl.ExtraDataInitGameExtensionImpl;
import com.io.begstd.dice.services.extension.common.impl.JackpotExtensionImpl;
import com.io.begstd.dice.services.extension.common.impl.PromotionExtensionImpl;
import com.io.begstd.dice.services.extension.common.impl.ResumeExtensionImpl;
import com.io.begstd.dice.services.extension.common.impl.SpinExtensionImpl;
import com.io.begstd.dice.services.extension.common.impl.SpinListenerExtensionImpl;
import com.io.begstd.dice.services.extension.common.impl.SubPlaySessionExtensionImpl;
import com.io.begstd.dice.services.extension.common.impl.WalletExtensionImpl;
import com.io.begstd.dice.services.extension.validator.ValidationExtension;
import com.io.begstd.dice.services.extension.validator.impl.ValidationExtensionImpl;
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
public class DiceExtensionManagerImpl implements com.io.begstd.extension.manager.ExtensionManager {

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

    private ExtraDataInitGameExtension extraDataInitGameExtension;
    
    public DiceExtensionManagerImpl() {
        extraDataInitGameExtension =  new ExtraDataInitGameExtensionImpl();
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

        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(GameExtension.class));
        for (BeanDefinition bd : scanner.findCandidateComponents("com.io.begstd.dice.services.extension")) {
            try {
                Class<?> clazz = Class.forName(bd.getBeanClassName());
                GameExtension ext = clazz.getAnnotation(GameExtension.class);
                registerExtension(ext.extOf(), clazz);
            } catch (ClassNotFoundException | InstantiationException | InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
                log.error(e.getMessage(), e);
            }
        }
    }

    public void verify(List<IDiceMachineConfig> iDiceMachineConfigs, GameRuleFactory gameRuleFactory) {
        wonRuleExtensionManager.verify(iDiceMachineConfigs, gameRuleFactory);
    }

    public DiceExtensionManagerImpl cloneObject() {
        return DiceExtensionManagerImpl.builder()
                .extraDataInitGameExtension(extraDataInitGameExtension)
                .denominationExtension(denominationExtension)
                .jackpotExtension(jackpotExtension)
                .playSessionExtension(playSessionExtension)
                .promotionExtension(promotionExtension)
                .spinListenerExtension(spinListenerExtension)
                .subPlaySessionExtension(subPlaySessionExtension)
                .walletExtension(walletExtension)
                .wonRuleExtensionManager(wonRuleExtensionManager.clone())
                .validationExtension(validationExtension)
                .spinExtension(spinExtension)
                .resumeExtension(resumeExtension)
                .build();

    }

    public void registerExtension(String type, Class clazz) throws NoSuchMethodException, IllegalAccessException,
            InvocationTargetException, InstantiationException {
        switch (type) {
            case "ExtraDataInitGameExtension":
                extraDataInitGameExtension = (ExtraDataInitGameExtension) clazz.getDeclaredConstructor().newInstance();
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
