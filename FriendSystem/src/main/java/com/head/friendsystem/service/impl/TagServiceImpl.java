package com.head.friendsystem.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.head.friendsystem.model.domain.Tag;
import com.head.friendsystem.service.TagService;
import com.head.friendsystem.mapper.TagMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
* @author headhead
* @description 针对表【tag(标签)】的数据库操作Service实现
* @createDate 2024-06-24 18:24:23
*/
@Service
public class TagServiceImpl extends ServiceImpl<TagMapper, Tag>
    implements TagService{
    @Resource
    private TagMapper tagMapper;
}




