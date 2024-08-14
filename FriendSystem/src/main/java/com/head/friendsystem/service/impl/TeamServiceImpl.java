package com.head.friendsystem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.head.friendsystem.common.ErrorCode;
import com.head.friendsystem.exception.BusinessException;
import com.head.friendsystem.mapper.TeamMapper;
import com.head.friendsystem.mapper.UserTeamMapper;
import com.head.friendsystem.model.domain.Team;
import com.head.friendsystem.model.domain.User;
import com.head.friendsystem.model.domain.UserTeam;
import com.head.friendsystem.model.dto.TeamQuery;
import com.head.friendsystem.model.enums.TeamStatusEnum;
import com.head.friendsystem.model.request.TeamJoinRequest;
import com.head.friendsystem.model.request.TeamQuitRequest;
import com.head.friendsystem.model.request.TeamUpdateRequest;
import com.head.friendsystem.model.vo.TeamUserVO;
import com.head.friendsystem.model.vo.UserVO;
import com.head.friendsystem.service.TeamService;
import com.head.friendsystem.service.UserService;
import com.head.friendsystem.service.UserTeamService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;


@Service
public class TeamServiceImpl extends ServiceImpl<TeamMapper, Team>
    implements TeamService {

    @Resource
    private UserTeamService userTeamService;

    @Resource
    private UserService userService;

    @Resource
    private UserTeamMapper userTeamMapper;

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

        //	1. 队伍人数 ＞1 且 ≤ 20
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
        //  4. status 是否公开 不传参数则默认为 0 (公开)
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
    public List<TeamUserVO> listTeams(TeamQuery teamQuery, boolean isAdmin) {
        QueryWrapper<Team> queryWrapper = new QueryWrapper<>();
        //  如果 teamQuery == null 则为查询所有队伍
        if(teamQuery != null){
            // 根据队伍 id 来查询
            Long id = teamQuery.getId();
            if(id != null){
                queryWrapper.eq("id", id);
            }
            List<Long> idList = teamQuery.getIdList();
            if(CollectionUtils.isNotEmpty(idList)){
                queryWrapper.in("id",idList);
            }
            String searchText = teamQuery.getSearchText();
            if(StringUtils.isNotBlank(searchText)){
                queryWrapper.and(qw -> qw.like("name", searchText).or().like("description", searchText));
            }
            // 根据队伍名称来查询
            String name = teamQuery.getName();
            if(StringUtils.isNotBlank(name)){
                queryWrapper.like("name", name);
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
            TeamStatusEnum statusEnum = TeamStatusEnum.getEnumByValue(status);
            // 若状态为空，则为默认(公开)
            if(statusEnum == null){
                statusEnum = TeamStatusEnum.PUBLIC;
            }
            if(!isAdmin && !statusEnum.equals(TeamStatusEnum.PUBLIC)){
                throw  new BusinessException(ErrorCode.NO_AUTH, "无权限！");
            }
            queryWrapper.eq("status", status);
        }
        // 不显示已经过期的队伍
        // expireTime == null or expireTime > now()  ==> 显示
        queryWrapper.and(qw -> qw.gt("expire_time",new Date()).or().isNull("expire_time"));
        List<Team> teamList = this.list(queryWrapper);
        // 如果啥都没查到，直接返回
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
            // 脱敏用户信息(密码)
            User safetyUser = userService.getSafetyUser(user);
            TeamUserVO teamUserVO = new TeamUserVO();
            BeanUtils.copyProperties(team, teamUserVO);// 把 team 中能拷贝的全拷贝到 teamUserVO
            if(user != null){
                UserVO userVO = new UserVO();
                BeanUtils.copyProperties(user, userVO);
                teamUserVO.setCreateUser(userVO);
            }
            teamUserVOList.add(teamUserVO);
        }
        return teamUserVOList;
    }

    @Override
    public boolean updateTeam(TeamUpdateRequest teamUpdateRequest, User loginUser) {
        if(teamUpdateRequest == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long id = teamUpdateRequest.getId();
        if(id == null || id <= 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Team oldTeam = this.getById(id);
        if(oldTeam == null){
            throw new BusinessException(ErrorCode.NULL_ERROR, "队伍不存在");
        }
        if(!oldTeam.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)){
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        TeamStatusEnum statusEnum = TeamStatusEnum.getEnumByValue(teamUpdateRequest.getStatus());
        if(statusEnum.equals(TeamStatusEnum.SECRET)){
            if(StringUtils.isBlank(teamUpdateRequest.getPassword())){
                throw new BusinessException(ErrorCode.PARAMS_ERROR,"加密房间必须设置密码");
            }
        }
        Team updateTeam = new Team();
        BeanUtils.copyProperties(teamUpdateRequest, updateTeam);
        return this.updateById(updateTeam);
    }

    @Override
    public boolean joinTeam(TeamJoinRequest teamJoinRequest, User loginUser) {
        if (teamJoinRequest == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long teamId = teamJoinRequest.getTeamId();
        Team team = getTeamById(teamId);
        Date expireTime = team.getExpireTime();
        if(expireTime != null && expireTime.before(new Date())){
            throw new BusinessException(ErrorCode.NULL_ERROR, "队伍已过期");
        }
        Integer status = team.getStatus();
        TeamStatusEnum teamStatusEnum = TeamStatusEnum.getEnumByValue(status);
        if(TeamStatusEnum.PRIVATE.equals(teamStatusEnum)){
            throw new BusinessException(ErrorCode.NULL_ERROR, "禁止加入私有队伍");
        }
        String password = teamJoinRequest.getPassword();
        if(TeamStatusEnum.SECRET.equals(teamStatusEnum)){
            if(StringUtils.isBlank(password) || !password.equals(team.getPassword())){
                throw new BusinessException(ErrorCode.NULL_ERROR, "密码错误");
            }
        }
        // 把需要查询数据库的操作放到后面，减少查询次数

        // 查询该用户加入了多少个队伍
        long userId = loginUser.getId();
        QueryWrapper<UserTeam> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        long hasJoinNum = userTeamService.count(queryWrapper);
        if(hasJoinNum > 5){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"最多创建和加入5个队伍");
        }

        // 不能重复加入已经加入的队伍
        queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId); // 查询该用户加入了多少个队伍
        queryWrapper.eq("team_id",teamId); // 查询该队伍加入了多少个人
        long hasUserJoinTeam = userTeamService.count(queryWrapper);
        if(hasUserJoinTeam > 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户已经加入该队伍");
        }

        // 查询该队伍加入了多少个人
        long teamHasJoinNum = this.countTeamUserByTeamId(teamId);
        if(teamHasJoinNum >= team.getMaxNum()){
            throw new BusinessException(ErrorCode.NULL_ERROR, "队伍已满");
        }
        // 修改队伍信息(用户队伍关联表)
        UserTeam userTeam = new UserTeam();
        userTeam.setUserId(userId);
        userTeam.setTeamId(teamId);
        userTeam.setJoinTime(new Date());
        return userTeamService.save(userTeam);
    }

    @Override
    public boolean quitTeam(TeamQuitRequest teamQuitRequest, User loginUser) {
        if(teamQuitRequest == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long teamId = teamQuitRequest.getTeamId();
        Team team = getTeamById(teamId);
        Long userId = loginUser.getId();
        UserTeam queryUserTeam = new UserTeam();
        queryUserTeam.setTeamId(teamId);
        queryUserTeam.setUserId(userId);
        QueryWrapper<UserTeam> queryWrapper = new QueryWrapper<>(queryUserTeam);
        long count = userTeamService.count(queryWrapper);
        if(count == 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不在该队伍中！");
        }
        long teamHasJoinNum = this.countTeamUserByTeamId(teamId);
        if(teamHasJoinNum == 1){
            // 队伍只剩下一人，解散
            this.removeById(teamId);
            queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("team_id",teamId);
        } else{
            // 判断是否为队长
            if(Objects.equals(team.getUserId(), userId)){
                // 把队长转移给第二早加入的用户
                queryWrapper = new QueryWrapper<>();
                queryWrapper.eq("team_id",teamId);
                queryWrapper.last("order bt id asc limit 2");
                List<UserTeam> userTeamList = userTeamService.list(queryWrapper);
                if(CollectionUtils.isEmpty(userTeamList) || userTeamList.size() <= 1){
                   throw new BusinessException(ErrorCode.SYSTEM_ERROR);
                }
                UserTeam nextUserTeam = userTeamList.get(1);
                Long nextTeamLeaderId = nextUserTeam.getUserId();
                // 更新当前队伍的队长
                Team updateTeam = new Team();
                updateTeam.setId(teamId);
                updateTeam.setUserId(nextTeamLeaderId);
                boolean result = this.updateById(updateTeam);
                if(!result){
                    throw new BusinessException(ErrorCode.SYSTEM_ERROR,"更新队长失败");
                }
                queryWrapper = new QueryWrapper<>();
                queryWrapper.eq("team_id", teamId);
                queryWrapper.eq("user_id", userId);
            }
        }
        return userTeamService.remove(queryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteTeam(long id, User loginUser) {
        // 1. 校验请求参数
        if(id <= 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 2. 校验队伍是否存在
        Team team = getTeamById(id);
        // 3. 校验用户是否为队长
        if(!Objects.equals(team.getUserId(), loginUser.getId())){
            throw new BusinessException(ErrorCode.NO_AUTH,"无权限");
        }
        // 4. 移除所有加入队伍的关联信息
        Long teamId = team.getId();
        QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
        userTeamQueryWrapper.eq("team_id", teamId);
        boolean result = userTeamService.remove(userTeamQueryWrapper);
        if(!result){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"删除队伍关联信息失败！");
        }
        // 5. 删除队伍
        return this.removeById(teamId);
    }




    /**
     * 根据队伍 id 获取队伍信息
     * @param teamId 队伍ID
     * @return 队伍
     */
    private Team getTeamById(Long teamId) {
        if(teamId == null || teamId <= 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Team team = this.getById(teamId);
        if(team == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍不存在");
        }
        return team;
    }

    /**
     * 查询该队伍有多少人
      * @param teamId 队伍Id
     * @return 队伍人数
     */
    private long countTeamUserByTeamId(long teamId){
        QueryWrapper<UserTeam> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("team_id",teamId);
        return userTeamService.count(queryWrapper);
    }

}




