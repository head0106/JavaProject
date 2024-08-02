package com.head.friendsystem.mapper;

import com.head.friendsystem.model.domain.UserTeam;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
* @author headhead
* @description 针对表【user_team(用户队伍关系)】的数据库操作Mapper
* @createDate 2024-07-31 15:02:30
* @Entity generator.domain.UserTeam
*/
@Mapper
public interface UserTeamMapper extends BaseMapper<UserTeam> {

}




