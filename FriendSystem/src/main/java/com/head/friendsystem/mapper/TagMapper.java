package com.head.friendsystem.mapper;

import com.head.friendsystem.model.domain.Tag;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
* @author headhead
* @description 针对表【tag(标签)】的数据库操作Mapper
* @createDate 2024-06-24 18:24:23
* @Entity generator.domain.Tag
*/

@Mapper
public interface TagMapper extends BaseMapper<Tag> {

}




