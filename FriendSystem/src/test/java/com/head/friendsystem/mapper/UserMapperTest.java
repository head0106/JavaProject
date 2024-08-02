package com.head.friendsystem.mapper;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

import static org.junit.Assert.*;

@SpringBootTest
public class UserMapperTest {
    @Resource
    private UserMapper userMapper;
    @Test
    public void selectCountByAccountTest(){
        String account = "zhangsan";

        System.out.println();
    }

}