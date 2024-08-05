package com.head.friendsystem.service;

import com.head.friendsystem.model.domain.User;
import com.baomidou.mybatisplus.extension.service.IService;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
* @author headhead
* @description 针对表【user(用户表)】的数据库操作Service
* @createDate 2024-06-23 17:18:17
*/

public interface UserService extends IService<User> {

    Long Register(String account,String password,String checkPassword,String planetCode);

    User login(String account, String password, HttpServletRequest request);

    List<User> search();

    User current(Long id);

    Integer logout(HttpServletRequest request);

    User getSafetyUser(User originUser);

    List<User> searchUsersByTags(List<String> tagNameList);

    Integer updateUser(User user, User loginUser);

    boolean isAdmin(HttpServletRequest request);

    boolean isAdmin(User user);

    User getLoginUser(HttpServletRequest request);
}
