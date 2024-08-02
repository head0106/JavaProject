package com.head.friendsystem.common;

import lombok.Data;

import java.io.Serializable;


/**
 * 通用返回类
 */
@Data
public class BaseResponse<T> implements Serializable {

    private Integer code; // 状态码

    private T data; // 数据

    private String message; // 消息

    private String description; // 描述（更具体）

    public BaseResponse(Integer code, T data, String message, String description) {
        this.code = code;
        this.data = data;
        this.message = message;
        this.description = description;
    }

    public BaseResponse(int code, T data, String message) {
        this(code, data, message, "");
    }
    public BaseResponse(int code, T data) {
        this(code, data, "", "");
    }

    //在Java中，如果一个构造函数想要调用同一个类中的另一个构造函数，必须使用 this() 来实现
    //并且这个调用必须是构造函数中的第一条语句
    public BaseResponse(ErrorCode errorCode) {
        this(errorCode.getCode(), null, errorCode.getMessage(), errorCode.getDescription());
    }

}
