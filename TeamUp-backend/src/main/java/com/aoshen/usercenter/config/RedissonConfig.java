package com.aoshen.usercenter.config;

import lombok.Data;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Redisson 配置
 *      Redisson 就是能让程序员像操作Java数据类型一样操作redis，
 *      同时解决了
 *          1. 方式执行时间过长，导致锁过期了然而还在执行之前的方法，这样就会让别的服务又得到了这把索，导致后面出现的一系列问题
 *              1）连锁效应：释放掉别人的锁
 *              2）这样还是会存在多个方法同时执行的情况
 *          2. 释放锁的时候，有可能先判断出是自己的锁，但这时锁过期了，最后还是释放了别人的锁
 * 2.
 */
@Configuration
@ConfigurationProperties(prefix = "spring.redis")
@Data
public class RedissonConfig {

    private String host;
    private String port;
    private String password;

    @Bean
    public RedissonClient redissonClient(){
        //1、创建配置
        Config config = new Config();
        String redisAddress = String.format("redis://%s:%s", host, port);
        config.useSingleServer().setAddress(redisAddress).setDatabase(0).setPassword(password);

        //2.创建实例
        return Redisson.create(config);
    }
}