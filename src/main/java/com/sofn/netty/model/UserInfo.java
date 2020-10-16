package com.sofn.netty.model;

import lombok.Data;

@Data
public class UserInfo {
    private String id;
    private String name;
    private String sex;
    private int age;
    private String remark;
}
