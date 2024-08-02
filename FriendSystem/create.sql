
use head;

-- 用户表
create table user
(
    id            bigint auto_increment
        primary key,
    user_account  varchar(256)                       not null,
    user_password varchar(512)                       not null,
    user_name     varchar(256)                       null,
    avatar_url    varchar(1024)                      null comment '头像的地址',
    gender        tinyint                            null comment '性别',
    phone         varchar(128)                       null,
    email         varchar(512)                       null,
    user_status   tinyint                            null comment '用户状态',
    tags          varchar(1024)                      null comment '标签列表',
    planet_code   varchar(512)                       null,
    user_role     int                                null,
    create_time   datetime default CURRENT_TIMESTAMP not null,
    update_time   datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP,
    is_delete     tinyint  default 0                 not null comment '0-未被删除，1-已被删除'
)
    comment '用户表' charset = utf8mb4;




-- 队伍表
create table team
(
    id          bigint auto_increment comment 'id'
        primary key,
    name        varchar(256)                       not null comment '队伍名称',
    description varchar(1024)                      null comment '描述',
    max_num     int      default 1                 not null comment '最大人数',
    expire_time datetime                           null comment '过期时间',
    user_id     bigint                             null comment '用户id（队长 id）',
    status      int      default 0                 not null comment '0 - 公开，1 - 私有，2 - 加密',
    password    varchar(512)                       null comment '密码',
    create_time datetime default CURRENT_TIMESTAMP null comment '创建时间',
    update_time datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP,
    is_delete   tinyint  default 0                 not null comment '是否删除'
)
    comment '队伍' charset = utf8mb4;

-- 标签表
create table tag
(
    id          bigint auto_increment
        primary key,
    tag_name    varchar(256)                       not null,
    user_id     bigint                             null comment '由哪个用户创建',
    parent_id   bigint                             null,
    is_parent   tinyint                            null comment '是否为父标签',
    create_time datetime default CURRENT_TIMESTAMP not null,
    update_time datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP,
    is_delete   tinyint  default 0                 not null comment '0-未被删除，1-已被删除',
    constraint tag_name
        unique (tag_name) comment '标签名称索引'
)
    comment '标签' charset = utf8mb4;

create index tag_user_id
    on tag (user_id);

-- 用户队伍关系表
create table user_team
(
    id          bigint auto_increment comment 'id'
        primary key,
    user_id     bigint                             null comment '用户id',
    team_id     bigint                             null comment '队伍id',
    join_time   datetime                           null comment '加入时间',
    create_time datetime default CURRENT_TIMESTAMP null comment '创建时间',
    update_time datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP,
    is_delete   tinyint  default 0                 not null comment '是否删除'
)
    comment '用户队伍关系' charset = utf8mb4;
