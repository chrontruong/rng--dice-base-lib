package com.io.begstd.slot.config;

import com.io.begstd.slot.services.matrix.generate.IGenerateMatrix;
import com.io.begstd.slot.services.matrix.generate.impl.BaseGenerateMatrix;
import com.io.begstd.slot.services.matrix.transform.ITransformMatrixService;
import com.io.begstd.slot.services.matrix.transform.impl.BaseTransformMatrixService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MatrixGenerateConfiguration {
    @Bean
    @ConditionalOnMissingBean(name = "normalGameGenerateMatrix")
    public IGenerateMatrix normalGameGenerateMatrix() {
        return new BaseGenerateMatrix();
    }
    
    @Bean
    @ConditionalOnMissingBean(name = "freeGameGenerateMatrix")
    public IGenerateMatrix freeGameGenerateMatrix() {
        return new BaseGenerateMatrix();
    }
    
    
    @Bean
    @ConditionalOnMissingBean(name = "normalGameTransformMatrix")
    public ITransformMatrixService normalGameTransformMatrix() {
        return new BaseTransformMatrixService();
    }
    
    @Bean
    @ConditionalOnMissingBean(name = "freeGameTransformMatrix")
    public ITransformMatrixService freeGameTransformMatrix() {
        return new BaseTransformMatrixService();
    }
    
}
