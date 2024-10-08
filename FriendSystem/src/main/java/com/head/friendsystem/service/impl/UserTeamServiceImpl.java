package com.head.friendsystem.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.head.friendsystem.model.domain.UserTeam;
import com.head.friendsystem.service.UserTeamService;
import com.head.friendsystem.mapper.UserTeamMapper;
import org.springframework.stereotype.Service;

/**
* @author headhead
* @description 针对表【user_team(用户队伍关系)】的数据库操作Service实现
* @createDate 2024-07-31 15:02:30
*/
@Service
public class UserTeamServiceImpl extends ServiceImpl<UserTeamMapper, UserTeam>
    implements UserTeamService{

}




