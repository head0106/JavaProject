package com.head.friendsystem.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.head.friendsystem.common.ErrorCode;
import com.head.friendsystem.exception.BusinessException;
import com.head.friendsystem.mapper.TeamMapper;
import com.head.friendsystem.model.domain.Team;
import com.head.friendsystem.model.domain.User;
import com.head.friendsystem.model.domain.UserTeam;
import com.head.friendsystem.model.dto.TeamQuery;
import com.head.friendsystem.model.enums.TeamStatusEnum;
import com.head.friendsystem.model.vo.TeamUserVO;
import com.head.friendsystem.model.vo.UserVO;
import com.head.friendsystem.service.TeamService;
import com.head.friendsystem.service.UserService;
import com.head.friendsystem.service.UserTeamService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;


@Service
public class TeamServiceImpl extends ServiceImpl<TeamMapper, Team>
    implements TeamService {

    @Resource
    private UserTeamService userTeamService;

    @Resource
    private UserService userService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public long addTeam(Team team, User loginUser) {
        //1. 请求参数是否为空？
        if(team == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"参数为空");
        }
        //2. 用户是否登录，未登录不允许新建
        if(loginUser == null){
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        //3. 校验信息

        //	1. 队伍人数＞1 且 ≤ 20
        Integer maxNum = team.getMaxNum();
        if(maxNum == null || maxNum < 1 || maxNum > 20){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"队伍人数错误！");
        }
        //	2. 队伍标题 ≤ 20
        String name = team.getName();
        if(name == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"队伍标题为空！");
        }
        if(name.length() > 20){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"队伍标题过长！");
        }
        //	3. 描述 ≤ 512
        String description = team.getDescription();
        if(StringUtils.isNotBlank(description) && description.length() > 512){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"队伍描述过长！");
        }
        //	4. status 是否公开 不传参数则默认为 0 (公开)
        int status = Optional.ofNullable(team.getStatus()).orElse(0);
        TeamStatusEnum teamStatusEnum = TeamStatusEnum.getEnumByValue(status);
        if(teamStatusEnum == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"队伍状态错误！");
        }
        //	5. 如果 status 是加密状态，则必须设置密码
        String password = team.getPassword();
        if(TeamStatusEnum.SECRET.equals(teamStatusEnum) && (StringUtils.isBlank(password) || password.length() > 32)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"密码有误！");
        }
        //	6. 超时时间＞当前时间
        Date expireTime = team.getExpireTime();
        if(new Date().after(expireTime)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"超时时间大于当前时间");
        }
        //	7. 校验用户最多创建 5 个队伍
        final long userId = team.getUserId();
        QueryWrapper<Team> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id",userId);
        long hasTeamNum = this.count(queryWrapper);
        if(hasTeamNum >= 5){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户最多创建 5 个队伍");
        }
        //4. 插入队伍信息到队伍表
        boolean result = this.save(team);
        //5. 插入用户与队伍关系到关系表
        Long teamId = team.getId();
        if(!result || teamId == null){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"创建队伍失败");
        }
        UserTeam userTeam = new UserTeam();
        userTeam.setUserId(userId);
        userTeam.setTeamId(teamId);
        userTeam.setJoinTime(new Date());
        result = userTeamService.save(userTeam);
        if(!result){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"创建队伍失败");
        }
        return team.getId();
    }

    @Override
    public List<TeamUserVO> listTeams(TeamQuery teamQuery) {
        QueryWrapper<Team> queryWrapper = new QueryWrapper<>();
        //  如果 teamQuery == null 则为查询所有队伍
        if(teamQuery != null){
            // 根据队伍 id 来查询
            Long id = teamQuery.getId();
            if(id != null){
                queryWrapper.eq("id", id);
            }
            // 根据队伍名称来查询
            String name = teamQuery.getName();
            if(StringUtils.isNotBlank(name)){
                queryWrapper.like("name",name);
            }
            // 根据队伍描述来查询
            String description = teamQuery.getDescription();
            if(StringUtils.isNotBlank(description)){
                queryWrapper.eq("description",description);
            }
            // 根据队伍最大人数来查询
            Integer maxNum = teamQuery.getMaxNum();
            if(maxNum != null && maxNum > 0){
                queryWrapper.eq("maxNum",maxNum);
            }
            // 根据用户(队长)Id来查询
            Long userId = teamQuery.getUserId();
            if(userId != null && userId > 0){
                queryWrapper.eq("user_id",userId);
            }
            // 根据队伍状态来查询
            Integer status = teamQuery.getStatus();
            if(status != null && status > -1){
                queryWrapper.eq("status",status);
            }
        }
        List<Team> teamList = this.list(queryWrapper);
        if(CollectionUtils.isEmpty(teamList)){
            return new ArrayList<>();
        }
        List<TeamUserVO> teamUserVOList = new ArrayList<>();
        // 管理查询创建人的用户信息
        for(Team team : teamList){
            Long userId = team.getUserId();
            if(userId == null){
                continue;
            }
            User user = userService.getById(userId);
            // 脱敏用户信息
            User safetyUser = userService.getSafetyUser(user);
            TeamUserVO teamUserVO = new TeamUserVO();
            BeanUtils.copyProperties(teamUserVO,team);
            UserVO userVO = new UserVO();
            BeanUtils.copyProperties(user, userVO);
            teamUserVO.setCreateUser(userVO);
            teamUserVOList.add(teamUserVO);
        }
        return teamUserVOList;
    }
}




