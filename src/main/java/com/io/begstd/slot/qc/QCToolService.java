package com.io.begstd.slot.qc;


import com.io.begstd.log.LogMessage;
import com.io.begstd.log.LogsUtils;
import com.io.begstd.slot.model.config.ISlotMachineConfig;
import com.io.begstd.slot.model.config.SlotConfigMode;
import com.io.begstd.slot.utils.BeanUtils;
import com.io.begstd.slot.utils.ConfigManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.web.servlet.context.AnnotationConfigServletWebServerApplicationContext;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@Profile("!production")
public class QCToolService implements ApplicationContextAware {

    private static ApplicationContext context;
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        context = applicationContext;

    }

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        ISlotMachineConfig config = (ISlotMachineConfig) BeanUtils.getBean(ConfigManager.class).getConfigMain(SlotConfigMode.NORMAL);
        String zClass = "com.io.bedstg.qctool.loader.TcPluginLoader";
        if (isClassExists(zClass)) {
            ConfigurableApplicationContext configurableContext = (ConfigurableApplicationContext) context;
            BeanDefinitionRegistry registry = (BeanDefinitionRegistry) configurableContext.getBeanFactory();
            BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.rootBeanDefinition(zClass);
            registry.registerBeanDefinition("qctool", beanDefinitionBuilder.getBeanDefinition());
            List<Object> arguments = new ArrayList();
            arguments.add(config.serviceId());
            arguments.add(this.context);
            Object obj = this.context.getBean("qctool");
            Method loader = null;
            try {
                loader = obj.getClass().getMethod("loader", List.class);
                loader.invoke(obj, arguments);
                LogMessage.LogMessageBuilder logBuilder = LogMessage.builder();
                logBuilder
                        .cmdId("")
                        .actorId("all")
                        .serviceId(config.serviceId())
                        .psId("")
                        .stateName("QCToolService.onApplicationReady")
                        .owner("SystemMonitor");
                LogsUtils.writeLogInfo(logBuilder.build());
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                log.debug("Can not load qc tool");
            }
        }
    }


    public boolean isClassExists(String className) {
        try {
            Class.forName(className);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
