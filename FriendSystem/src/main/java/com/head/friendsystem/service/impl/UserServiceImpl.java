package com.head.friendsystem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.head.friendsystem.common.ErrorCode;
import com.head.friendsystem.constant.UserConstant;
import com.head.friendsystem.exception.BusinessException;
import com.head.friendsystem.model.domain.User;
import com.head.friendsystem.service.UserService;
import com.head.friendsystem.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.head.friendsystem.constant.UserConstant.ADMIN_ROLE;
import static com.head.friendsystem.constant.UserConstant.USER_LOGIN_STATE;

/**
* @author headhead
* @description 针对表【user(用户表)】的数据库操作Service实现
* @createDate 2024-06-23 17:18:17
*/
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService{
    @Resource
    private UserMapper userMapper;
    private static final String SALT = "headishead";

    @Override
    public Long Register(String account, String password, String checkPassword,String planetCode) {
        // 已知，传进来的数据不可能为null
        if(account.length() < 4){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"账户过短");
        }
        if(password.length() < 8){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"密码过短");
        }
        if (planetCode.length() > 5) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"星球编号过长");
        }
        //账户和密码不能包含特殊字符
        String regEx="[`~!@#$%^&*()+=|{}':;',\\[\\]<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
        Matcher matcher = Pattern.compile(regEx).matcher(account + password);
        if(matcher.find()){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"账号和密码不能包含特殊字符!");
        }
        // 密码和校验密码相同
        if(!password.equals(checkPassword)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"两次输入的密码不一致");
        }

        //用户不能重复
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_account",account);
        Long count = userMapper.selectCount(queryWrapper);
        if(count > 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"账号已存在");
        }

        // 星球编号不能重复
        queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("planet_code", planetCode);
        count = userMapper.selectCount(queryWrapper);
        if(count > 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"星球编号重复!");
        }
        // 对密码加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + password).getBytes(StandardCharsets.UTF_8));
        User user = new User();
        user.setUserAccount(account);
        user.setUserPassword(encryptPassword);
        user.setPlanetCode(planetCode);
        int result = userMapper.insert(user);
        if(result <= 0){
            log.error("注册失败");
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"新建用户失败,请联系管理员");
        }
        return user.getId();
    }

    @Override
    public User login(String account, String password, HttpServletRequest request) {
        // 1.校验
        if(StringUtils.isAnyBlank(account,password)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"参数错误");
        }
        if(account.length() < 4){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"账号过短");
        }
        if(password.length() < 8){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"密码过短");
        }
        //账户不能包含特殊字符
        String regEx="[`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
        Matcher matcher = Pattern.compile(regEx).matcher(account);
        if(matcher.find()){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"账号和密码不能包含特殊字符!");
        }

        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + password).getBytes());
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_account", account);
        queryWrapper.eq("user_password",encryptPassword);
        User user= userMapper.selectOne(queryWrapper);
        if(user == null){
            // 用户不存在
            log.info("用户不存在");
            return null;
        }

        // todo 拓展：查看该用户登录次数是否太多

        // 3.用户脱敏
        User safetyUser = getSafetyUser(user);
        // 4.记录用户的登录态
        request.getSession().setAttribute(USER_LOGIN_STATE,safetyUser);
        System.out.println("输出的 Session:" + request.getSession());
        return safetyUser;
    }

    @Override
    public List<User> search() {
        return userMapper.selectBatchIds(Arrays.asList(1,2));
    }

    @Override
    public User current(Long id) {
        return null;
    }

    @Override
    public Integer logout(HttpServletRequest request) {
        // 移除登录态
        request.getSession().removeAttribute(USER_LOGIN_STATE);
        return 1;
    }

    // 脱敏
    @Override
    public User getSafetyUser(User originUser) {
        if(originUser == null){
            return null;
        }
        User safetyUser = new User();
        safetyUser.setId(originUser.getId());
        safetyUser.setUserName(originUser.getUserName());
        safetyUser.setAvatarUrl(originUser.getAvatarUrl());
        safetyUser.setGender(originUser.getGender());
        safetyUser.setUserAccount(originUser.getUserAccount());
        safetyUser.setUserPassword(""); //脱敏
        safetyUser.setUserRole(originUser.getUserRole());
        safetyUser.setPhone(originUser.getPhone());
        safetyUser.setEmail(originUser.getEmail());
        safetyUser.setUserStatus(originUser.getUserStatus());
        safetyUser.setCreateTime(originUser.getCreateTime());
        safetyUser.setIsDelete(originUser.getIsDelete());
        return safetyUser;
    }

    @Override
    public List<User> searchUsersByTags(List<String> tagNameList) {
        if (CollectionUtils.isEmpty(tagNameList)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
//        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
//        // 拼接 and 查询
//        // like '%Java%' and '%Python%'
//        for (String tagName : tagNameList){
//            queryWrapper = queryWrapper.like("tags",tagName);  // todo 不太理解，需要学习一下 Mybatis Plus 的 queryWrapper
//        }
//        List<User> userList = userMapper.selectList(queryWrapper);
//        return userList.stream().map(this::getSafetyUser).collect(Collectors.toList()); // todo 学习 Java8
        // 1. 先查询所有用户
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        List<User> userList = userMapper.selectList(queryWrapper);
        Gson gson = new Gson();
        // 2. 在内存中判断是否包含要求的标签
        return userList.stream().filter(user -> {
            String tagsStr = user.getTags();
            if(StringUtils.isBlank(tagsStr)){
                return false;
            }
            Set<String> tempTagNameSet = gson.fromJson(tagsStr, new TypeToken<Set<String>>(){}.getType());
                for(String tagName : tagNameList){
                    if(!tempTagNameSet.contains(tagName)){
                        return false;
                    }
            }
            return true;
        }).map(this::getSafetyUser).collect(Collectors.toList());



    }

    @Override
    public Integer updateUser(User user,User loginUser) {
        // 再判断一次
        if(user == null || loginUser == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        if(!Objects.equals(loginUser.getId(), user.getId()) || !Objects.equals(loginUser.getUserRole(), ADMIN_ROLE)){
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        Long id = user.getId();
        if(id == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Integer result = userMapper.updateById(user);
        if(result == null){

        }
        return null;
    }

    @Override
    public boolean isAdmin(HttpServletRequest request){
        //
        Object userObj = request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        User user = (User) userObj;
        return user != null && Objects.equals(user.getUserRole(), UserConstant.ADMIN_ROLE);
    }
    @Override
    public boolean isAdmin(User user){
        return user != null && Objects.equals(user.getUserRole(), UserConstant.ADMIN_ROLE);
    }

    @Override
    public User getLoginUser(HttpServletRequest request) {
        if(request == null){
            return null;
        }
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        if(userObj == null){
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        return (User) userObj;
    }
}




