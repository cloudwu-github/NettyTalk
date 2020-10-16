package com.sofn.netty.model;

import lombok.Data;

import java.io.Serializable;

/**
 * 发送netty小弟的对象
 * @param <T>
 */
@Data
public class SendMessage<T> implements Serializable {
    //发送消息的id,唯一编号
    private String id;
    //发送消息的类型：根据类型来获取相应的对象
    private String type;
    //发送消息的对象
    private T data;
}
