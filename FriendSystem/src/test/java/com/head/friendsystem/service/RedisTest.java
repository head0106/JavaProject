package com.head.friendsystem.service;

import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import javax.annotation.Resource;

@SpringBootTest
public class RedisTest {

    @Resource
    private RedisTemplate redisTemplate;
    @Test
    public void test(){
        ValueOperations valueOperations = redisTemplate.opsForValue();
        valueOperations.set("headString","monkey");
        valueOperations.set("headInt",1);


    }
}
