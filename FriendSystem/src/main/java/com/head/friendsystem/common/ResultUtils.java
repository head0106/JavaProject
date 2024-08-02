package com.head.friendsystem.common;


/**
 * 返回工具类
 */

public class ResultUtils {

    /**
     * 成功
     * @param data 任意
     * @return BaseResponse
     * @param s
     */
    public static <T> BaseResponse<T> success(T data){
        return new BaseResponse<>(0,data,"ok");
    }
    public static BaseResponse error(ErrorCode errorCode){
        return new BaseResponse<>(errorCode);
    }
    public static BaseResponse error(int code, String message, String description){
        return new BaseResponse(code, null,message,description);
    }

    public static BaseResponse error(ErrorCode errorCode, String message, String description){
        return new BaseResponse(errorCode.getCode(),null,message,description);
    }
    public static BaseResponse error(ErrorCode errorCode, String description) {
        return new BaseResponse(errorCode.getCode(), errorCode.getMessage(), description);
    }
}


