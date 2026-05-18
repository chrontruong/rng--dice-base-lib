package com.io.begstd.extension.config;

import com.io.begstd.extension.loader.ExtensionLoader;
import org.springframework.context.annotation.Bean;

public class ExtensionLoaderBeanConfig {


    @Bean
    public ExtensionLoader<?> loader() {
        return new ExtensionLoader();
    }

}
