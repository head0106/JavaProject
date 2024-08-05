package com.head.friendsystem.model.request;


import lombok.Data;

/**
 * (用户)加入队伍封装类
 */
@Data
public class TeamJoinRequest {
    /**
     *  队伍 ID
     */
    private Long teamId;
    /**
     * 密码
     */
    private String password;
}

