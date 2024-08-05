package com.head.friendsystem.model.vo;

import lombok.Data;

import java.util.Date;

/**
 * 用户包装类
 * @author head
 */

@Data
public class UserVO {
    /**
     *
     */
    private Long id;

    /**
     *
     */
    private String userName;

    /**
     *
     */
    private String userAccount;

    /**
     * 标签列表
     */
    private String tags;

    /**
     * 头像的地址
     */
    private String avatarUrl;

    /**
     * 性别
     */
    private Integer gender;

    /**
     *
     */
    private String phone;

    /**
     *
     */
    private String email;

    /**
     * 用户状态
     */
    private Integer userStatus;

    /**
     *
     */
    private Integer userRole;

    /**
     *
     */
    private String planetCode;

    /**
     *
     */
    private Date createTime;

    /**
     *
     */
    private Date updateTime;
}
