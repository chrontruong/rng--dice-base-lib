package com.io.begstd.slot.utils;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class BeanUtils implements ApplicationContextAware {

    private static ApplicationContext context;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        context = applicationContext;

    }

    public static <T> T getBean(Class<T> beanClass) {
        if (context != null)
            return context.getBean(beanClass);
        return null;
    }
    public static <T> Map<String,T> getBeanOfType(Class<T> beanClass) {
        if (context != null)
            return context.getBeansOfType(beanClass);
        return null;
    }
    
    public static <T> T getBean(String name, Class<T> beanClass) {
        if (context != null)
            return context.getBean(name, beanClass);
        return null;
    }

}
