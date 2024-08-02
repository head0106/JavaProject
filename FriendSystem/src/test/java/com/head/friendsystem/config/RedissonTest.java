package com.head.friendsystem.config;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.redisson.Redisson;
import org.redisson.api.RList;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@SpringBootTest
@RunWith(SpringRunner.class)
public class RedissonTest {

    @Resource
    private RedissonClient redissonClient;
    @Test
    public void test(){
        // list
        List<String> list = new ArrayList<>();
        list.add("head");
        list.get(0);
        // list.remove(0);

        RList<String> rList = redissonClient.getList("test-list");
        rList.add("head");
        rList.get(0);
        rList.remove(0);


        RMap<Object, Object> rMap = redissonClient.getMap("test-map");
        rMap.put(1,"head");
        rMap.put(2,"value");

    }

}
