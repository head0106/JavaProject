package com.head.friendsystem.common;

import lombok.Getter;

@Getter
public enum ErrorCode {

    //每个枚举常量都是通过调用构造函数创建的
    SUCCESS(0,"ok",""),
    PARAMS_ERROR(40000,"请求参数错误",""),
    NULL_ERROR(40001,"请求数据为空",""),
    NOT_LOGIN(40100,"未登录",""),
    NO_AUTH(40101,"无权限",""),
    SYSTEM_ERROR(50000,"系统内部异常","");

    /*
    状态码
     */
    private final Integer code;
    /*
    状态码信息
     */
    private final String message;
    /*
    状态码信息详细版
    */
    private final String description;
    //枚举的构造函数是私有的
    ErrorCode(int code, String message, String description) {
        this.code = code;
        this.message = message;
        this.description = description;
    }
}
