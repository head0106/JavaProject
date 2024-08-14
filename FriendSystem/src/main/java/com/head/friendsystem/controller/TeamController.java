package com.head.friendsystem.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ser.Serializers;
import com.head.friendsystem.common.BaseResponse;
import com.head.friendsystem.common.DeleteRequest;
import com.head.friendsystem.common.ErrorCode;
import com.head.friendsystem.common.ResultUtils;
import com.head.friendsystem.exception.BusinessException;
import com.head.friendsystem.model.domain.Team;
import com.head.friendsystem.model.domain.User;
import com.head.friendsystem.model.domain.UserTeam;
import com.head.friendsystem.model.dto.TeamQuery;
import com.head.friendsystem.model.request.TeamAddRequest;
import com.head.friendsystem.model.request.TeamJoinRequest;
import com.head.friendsystem.model.request.TeamQuitRequest;
import com.head.friendsystem.model.request.TeamUpdateRequest;
import com.head.friendsystem.model.vo.TeamUserVO;
import com.head.friendsystem.service.TeamService;
import com.head.friendsystem.service.UserService;
import com.head.friendsystem.service.UserTeamService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


@RequestMapping("/team")
@RestController
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
@Slf4j
public class TeamController {
    @Resource
    private TeamService teamService;
    @Resource
    private UserService userService;
    @Resource
    private UserTeamService userTeamService;

    @PostMapping("/add")
    public BaseResponse<Long> addTeam(@RequestBody TeamAddRequest teamAddRequest, HttpServletRequest request){
        if (teamAddRequest == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Team team = new Team();
        BeanUtils.copyProperties(teamAddRequest,team);
        User loginUser = userService.getLoginUser(request);
        long teamId = teamService.addTeam(team, loginUser);
        return ResultUtils.success(teamId);
    }

    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteTeam(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request){
        long id = deleteRequest.getId();
        if(id <= 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        boolean result = teamService.deleteTeam(id, loginUser);
        if(!result){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"删除队伍失败");
        }
        return ResultUtils.success(true);
    }

    @PostMapping("/update")
    public BaseResponse<Boolean> updateTeam(@RequestBody TeamUpdateRequest teamUpdateRequest, HttpServletRequest request){
        if(teamUpdateRequest == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        if(loginUser == null){
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        boolean result = teamService.updateTeam(teamUpdateRequest, loginUser);
        if(!result){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"更新队伍失败");
        }
        return ResultUtils.success(true);
    }

    @PostMapping("/get")
    public BaseResponse<Team> getTeamById(@RequestBody long id){
        if(id <= 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Team team = teamService.getById(id);
        if(team == null){
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        return ResultUtils.success(team);
    }

    @GetMapping("/list")
    public BaseResponse<List<TeamUserVO>> listTeams(TeamQuery teamQuery, HttpServletRequest request){
        if(teamQuery == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean isAdmin = userService.isAdmin(request);
        List<TeamUserVO> teamList = teamService.listTeams(teamQuery, isAdmin);

        return ResultUtils.success(teamList);
    }

    @GetMapping("/list/page")
    public BaseResponse<Page<Team>> listTeamsByPage(TeamQuery teamQuery){
        if(teamQuery == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Team team = new Team();
        // 将 teamQuery 中的数据拷贝到 team 中
        BeanUtils.copyProperties(teamQuery,team);
        Page<Team> page = new Page<>(teamQuery.getPageNum(),teamQuery.getPageSize());
        QueryWrapper<Team> queryWrapper = new QueryWrapper<>(team);
        Page<Team> resultPage = teamService.page(page, queryWrapper);
        return ResultUtils.success(resultPage);
    }

    @PostMapping("/join")
    public BaseResponse<Boolean> joinTeam(@RequestBody TeamJoinRequest teamJoinRequest, HttpServletRequest request){
        if(teamJoinRequest == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        if(loginUser == null){
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        boolean result = teamService.joinTeam(teamJoinRequest, loginUser);
        return ResultUtils.success(result);
    }

    @PostMapping("/quit")
    public BaseResponse<Boolean> quitTeam(@RequestBody TeamQuitRequest teamQuitRequest, HttpServletRequest request){
        if(teamQuitRequest == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        if(loginUser == null){
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        boolean result = teamService.quitTeam(teamQuitRequest, loginUser);
        return ResultUtils.success(result);
    }

    /**
     * 获取用户创建的队伍
     * @param teamQuery
     * @param request
     * @return
     */
    @GetMapping("/list/my/create")
    public BaseResponse<List<TeamUserVO>> listMyCreateTeams(TeamQuery teamQuery, HttpServletRequest request){
        if(teamQuery == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        teamQuery.setUserId(loginUser.getId());
        List<TeamUserVO> teamList = teamService.listTeams(teamQuery, true);
        return ResultUtils.success(teamList);
    }

    /**
     * 获取用户加入的队伍
     * @param teamQuery
     * @param request
     * @return
     */
    @GetMapping("/list/my/join")
    public BaseResponse<List<TeamUserVO>> listMyJoinTeams(TeamQuery teamQuery, HttpServletRequest request){
        if(teamQuery == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        QueryWrapper<UserTeam> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", loginUser.getId());
        List<UserTeam> userTeamList = userTeamService.list(queryWrapper);
        // todo 不懂在干嘛
        Map<Long, List<UserTeam>> listMap = userTeamList.stream()
                .collect(Collectors.groupingBy(UserTeam::getTeamId));
        List<Long> idList = new ArrayList<>(listMap.keySet());
        teamQuery.setIdList(idList);
        List<TeamUserVO> teamList = teamService.listTeams(teamQuery, true);

        return ResultUtils.success(teamList);
    }


}
