package com.example.ureka02.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedissionConfig {
    @Bean
    public RedisTemplate<String, String> RedisTemplate(RedisConnectionFactory connectionFactory) {
        StringRedisTemplate redisTemplate = new StringRedisTemplate();
        redisTemplate.setConnectionFactory(connectionFactory);

        // Key 직렬화 설정 (문자열)
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        // Value 직렬화 설정 (문자열)
        redisTemplate.setValueSerializer(new StringRedisSerializer());

        return redisTemplate;
    }
}
