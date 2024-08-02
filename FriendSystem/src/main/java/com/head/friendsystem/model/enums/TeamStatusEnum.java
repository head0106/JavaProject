package com.head.friendsystem.model.enums;

import io.swagger.models.auth.In;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

/**
 * 队伍状态枚举
 */

public enum TeamStatusEnum {

    PUBLIC(0,"公开"),
    PRIVATE(1,"私有"),
    SECRET(2,"加密");

    public static TeamStatusEnum getEnumByValue(Integer value){
        if(value == null){
            return null;
        }
        TeamStatusEnum[] values = TeamStatusEnum.values();
        for(TeamStatusEnum teamStatusEnum : values){
            if(teamStatusEnum.value == value){
                return teamStatusEnum;
            }
        }
        return null;
    }

    private int value;
    private String text;
    TeamStatusEnum(int value, String text){
        this.text = text;
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
