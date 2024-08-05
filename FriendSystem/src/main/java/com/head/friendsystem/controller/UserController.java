package com.head.friendsystem.controller;



import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.head.friendsystem.common.BaseResponse;
import com.head.friendsystem.common.ErrorCode;
import com.head.friendsystem.common.ResultUtils;
import com.head.friendsystem.constant.UserConstant;
import com.head.friendsystem.exception.BusinessException;
import com.head.friendsystem.model.domain.User;
import com.head.friendsystem.model.request.UserLoginRequest;
import com.head.friendsystem.model.request.UserRegisterRequest;
import com.head.friendsystem.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.head.friendsystem.constant.UserConstant.ADMIN_ROLE;
import static com.head.friendsystem.constant.UserConstant.USER_LOGIN_STATE;

@RequestMapping("/user")
@RestController
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
@Slf4j
public class UserController {
    @Resource
    private UserService userService;

    @Resource
    private RedisTemplate<String,Object> redisTemplate;

    @PostMapping("/register")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest){
        String account = userRegisterRequest.getUserAccount();
        String password = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        String planetCode = userRegisterRequest.getPlanetCode();  // todo 后期改进：改为学号
        if(StringUtils.isAnyEmpty(account,password,checkPassword,planetCode)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long result = userService.Register(account,password,checkPassword,planetCode);
        return ResultUtils.success(result);
    }

    @PostMapping("/login")
    public BaseResponse<User> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request){
        String account = userLoginRequest.getUserAccount();
        String password = userLoginRequest.getUserPassword();
        if(StringUtils.isAnyEmpty(account,password)){
            return ResultUtils.error(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.login(account,password,request);
        return ResultUtils.success(user);
    }

    // 返回当前用户
    @GetMapping("/current")
    public BaseResponse<User> getCurrentUser(HttpServletRequest request, HttpServletResponse response){
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User currentUser = (User) userObj;
        if(currentUser == null){
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        long userId = currentUser.getId();
        // todo 校验用户是否合法
        User user = userService.getById(userId);
        User safetyUser = userService.getSafetyUser(user); // 用户信息脱敏
        return ResultUtils.success(safetyUser);
    }


    // 注销
    @PostMapping("/logout")
    public Integer userLogout(HttpServletRequest request){
        return userService.logout(request);
    }

    // 查询所有用户信息
    @GetMapping("/search")
    public BaseResponse<List<User>> userSearch(String userName, HttpServletRequest request){
        if(!userService.isAdmin(request)){
            throw new BusinessException(ErrorCode.NO_AUTH,"缺少管理员权限");
        }
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        if(StringUtils.isNotBlank(userName)){
            queryWrapper.like("user_name",userName);
        }
        List<User> userList = userService.list(queryWrapper);
        List<User> list = userList.stream().map(user -> userService.getSafetyUser(user)).collect(Collectors.toList());

        return ResultUtils.success(list);
    }
    @GetMapping("/recommend")
    public BaseResponse<Page<User>> recommendUsers(long pageSize, long pageNum, HttpServletRequest request){
        User loginUser = userService.getLoginUser(request);
        // 如果有缓存，直接读缓存
        String redisKey = String.format("head:user:recommend:%s",loginUser.getId());
        ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();
        Page<User> userPage = (Page<User>) redisTemplate.opsForValue().get(redisKey);
        if(userPage != null){
            return ResultUtils.success(userPage);
        }
        // 无缓存，查数据库
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        userPage = userService.page(new Page<>(pageNum, pageSize), queryWrapper);
        // 查询数据库后并写入缓存
        try{
            // 一定要设置过期时间！！！！！！
            valueOperations.set(redisKey,userPage,100000, TimeUnit.MILLISECONDS);
        }catch (Exception e){
            log.error("redis set key error", e);
        }

        return ResultUtils.success(userPage);
    }


    @GetMapping("/search/tags")
    public BaseResponse<List<User>> searchUsersByTags(@RequestParam(required = false ) List<String> tagNameList){
        if (CollectionUtils.isEmpty(tagNameList)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        List<User> userList = userService.searchUsersByTags(tagNameList);
        return ResultUtils.success(userList);
    }


    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteUser(@RequestBody long id, HttpServletRequest request) {
        if (!userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean b = userService.removeById(id);
        return ResultUtils.success(b);
    }
    @PostMapping("/update")
    public BaseResponse<Integer> updateUser(@RequestBody User user, HttpServletRequest request){
        //1. 校验参数是否为空
        if(user == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        if(request == null){
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        //2. 校验权限
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User loginUser = (User) userObj; // 此时 loginUser 不可能为空
        // user 是要修改的用户，loginUser 是登录用户
        // 如果登录用户既不是管理员也不是要修改信息的本人，那么无权限
        if(!userService.isAdmin(request) && !Objects.equals(loginUser.getId(), user.getId())){
            throw new BusinessException(ErrorCode.NO_AUTH);
        }

        //3. 触发更新
        Integer result = userService.updateUser(user,loginUser);
        return ResultUtils.success(result);
    }




}
