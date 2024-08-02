package com.head.friendsystem.model.request;

import lombok.Data;

@Data
public class UserLoginRequest {
    private String userAccount;
    private String userPassword;
}
