package com.head.friendsystem.mapper;

import com.head.friendsystem.model.domain.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
* @author headhead
* @description 针对表【user(用户表)】的数据库操作Mapper
* @createDate 2024-06-23 17:18:17
* @Entity generator.domain.User
*/
@Mapper
public interface UserMapper extends BaseMapper<User> {

}




