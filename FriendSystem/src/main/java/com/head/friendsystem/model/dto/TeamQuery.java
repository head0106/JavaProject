package com.head.friendsystem.model.dto;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.head.friendsystem.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;
import java.util.List;

/**
 * 队伍查询信息封装类
 */

@EqualsAndHashCode(callSuper = true)
@Data
public class TeamQuery extends PageRequest {

    /**
     * id
     */
    private Long id;
    /**
     * id 列表
     */
    private List<Long> idList;
    /**
     * 搜索关键词(同时对 name 和 description 进行搜索)
     */
    private String searchText;
    /**
     * 队伍名称
     */
    private String name;

    /**
     * 描述
     */
    private String description;

    /**
     * 最大人数
     */
    private Integer maxNum;

    /**
     * 过期时间
     */
    private Date expireTime;

    /**
     * 用户id（队长 id）
     */
    private Long userId;

    /**
     * 0 - 公开，1 - 私有，2 - 加密
     */
    private Integer status;

    /**
     * 密码
     */
    private String password;

}

