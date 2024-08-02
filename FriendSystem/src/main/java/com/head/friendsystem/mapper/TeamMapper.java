package com.head.friendsystem.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.head.friendsystem.model.domain.Team;
import org.apache.ibatis.annotations.Mapper;

/**
* @author headhead
* @description 针对表【team(队伍)】的数据库操作Mapper
* @createDate 2024-07-31 14:53:03
* @Entity generator.domain.Team
*/
@Mapper
public interface TeamMapper extends BaseMapper<Team> {

}




