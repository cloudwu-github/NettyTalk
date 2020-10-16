package com.sofn.netty.model;

import lombok.Data;

import java.io.Serializable;

@Data
public class MessageObj implements Serializable {
    //1:登录,2：对话
    private String type;
    private String fromUserId;
    private String toUserId;
    private String toGroupId;
    private String message;
}
