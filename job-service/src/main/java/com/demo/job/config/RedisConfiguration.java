package com.demo.job.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

@Configuration
public class RedisConfiguration {
    @Bean
    @Primary
    public StringRedisTemplate template(RedisConnectionFactory factory){
        return new StringRedisTemplate(factory);
    }
}
