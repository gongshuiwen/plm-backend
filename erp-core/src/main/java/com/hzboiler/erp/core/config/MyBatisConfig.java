package com.hzboiler.erp.core.config;

import com.baomidou.mybatisplus.autoconfigure.ConfigurationCustomizer;
import com.hzboiler.erp.core.field.Many2One;
import com.hzboiler.erp.core.mybatis.Many2OneTypeHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author gongshuiwen
 */
@Configuration
public class MyBatisConfig {

    @Bean
    public ConfigurationCustomizer mybatisConfigurationCustomizer() {
        return configuration -> configuration.getTypeHandlerRegistry().register(Many2One.class, new Many2OneTypeHandler());
    }
}