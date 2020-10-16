package com.sofn.netty.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.PropertyFilter;
import com.sofn.common.model.Result;
import com.sofn.common.utils.IdUtil;
import com.sofn.common.utils.JsonUtils;
import com.sofn.netty.model.SendMessage;
import com.sofn.netty.model.UserInfo;
import com.sofn.netty.server.NettyServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/netty")
public class NettyController {
    @Autowired
    private NettyServer nettyServer;

    @PostMapping("/sendMsg")
    public Result<String> sendMessage(){
        UserInfo userInfo=new UserInfo();
        userInfo.setId(IdUtil.getUUId());
        userInfo.setName("张三");
        userInfo.setSex("1");
        userInfo.setAge(26);
        userInfo.setRemark("高级工程师");

        SendMessage<UserInfo> sendMessage=new SendMessage<UserInfo>();
        sendMessage.setId(IdUtil.getUUId());
        sendMessage.setType("user");
        sendMessage.setData(userInfo);

        String userName="name";
        PropertyFilter propertyFilter=new PropertyFilter() {
            @Override
            public boolean apply(Object object, String name, Object value) {
                if(name.equalsIgnoreCase(userName)){
                    return false;
                }
                return true;
            }
        };

        System.out.println(JSON.toJSONString(sendMessage,propertyFilter));
        nettyServer.sendMessage(sendMessage);
        return Result.ok("群发成功！");
    }
}
