package com.head.friendsystem.service;

import com.head.friendsystem.model.domain.User;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

import java.util.Arrays;
import java.util.List;

@SpringBootTest
@RunWith(SpringRunner.class)
public class UserServiceTest {
    @Resource
    private UserService userService;
    @Test
    public void searchUsersByTags() {
        List<String> tagNameList = Arrays.asList("java","python");
        List<User> userList = userService.list();

        System.out.println("hello");
        List<User> userList1 = userService.searchUsersByTags(tagNameList);
        Assert.assertNotNull(userList);
    }
}