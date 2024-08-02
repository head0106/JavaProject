package com.head.friendsystem.model.domain;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 用户表
 * @TableName user
 */
@TableName(value ="user")
@Data
public class User implements Serializable {
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
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
     * 
     */
    private String userPassword;

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

    /**
     * 0-未被删除，1-已被删除
     */
    @TableLogic
    private Integer isDelete;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}