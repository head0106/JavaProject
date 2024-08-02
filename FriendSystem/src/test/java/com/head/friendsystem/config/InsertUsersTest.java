package com.head.friendsystem.config;

import com.head.friendsystem.mapper.UserMapper;
import com.head.friendsystem.model.domain.User;
import com.head.friendsystem.service.UserService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.StopWatch;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@SpringBootTest
@RunWith(SpringRunner.class)
public class InsertUsersTest {
    @Resource
    private UserMapper userMapper;
    @Resource
    private UserService userService;
    @Test
    public void doInsetUsers(){
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        final int INSERT_NUM = 100000;
        List<User> list = new ArrayList<>();
        System.out.println("start...");
        for(int i = 0; i < INSERT_NUM; i++){
            User user = new User();
            user.setUserName("假head");
            user.setUserAccount("fakeheadhead");
            user.setUserPassword("12345678");
            user.setTags("");
            user.setAvatarUrl("");
            user.setGender(0);
            user.setPhone("");
            user.setEmail("");
            user.setUserStatus(0);
            user.setUserRole(0);
            user.setPlanetCode("111111111111");
            list.add(user);
            System.out.println(user);
        }
        userService.saveBatch(list);
        System.out.println("end...");
        stopWatch.stop();
        System.out.println(stopWatch.getTotalTimeMillis());
    }
}
