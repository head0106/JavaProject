package com.head.friendsystem.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.head.friendsystem.model.domain.Team;
import com.head.friendsystem.model.domain.User;
import com.head.friendsystem.model.dto.TeamQuery;
import com.head.friendsystem.model.vo.TeamUserVO;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
* @author headhead
* @description 针对表【team(队伍)】的数据库操作Service
* @createDate 2024-07-31 14:53:03
*/
@Service
public interface TeamService extends IService<Team> {

    /**
     * 创建队伍
     * @param team 创建队伍的信息
     * @param loginUser 当前登录用户（队伍创建人）
     * @return 返回新建队伍的ID
     */
    long addTeam(Team team, User loginUser);

    /**
     * 查询队伍
     * @param teamQuery
     * @return
     */
    List<TeamUserVO> listTeams(TeamQuery teamQuery);
}
