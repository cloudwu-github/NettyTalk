package com.sofn.netty.websocket;

import com.google.common.collect.Lists;
import com.sofn.common.utils.JsonUtils;
import com.sofn.netty.model.MessageObj;
import io.netty.channel.*;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * webSocket 业务类
 * wuXY
 * 2020-10-15 17:00:17
 */
public class TextWebSocketFrameHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {

    //通道id的map
    private static ConcurrentHashMap<ChannelId, ChannelHandlerContext> channelIdMap = new ConcurrentHashMap<>();
    //通道id所在的用户map
    private static ConcurrentHashMap<String, Channel> channelUserNameMap = new ConcurrentHashMap<>();

    //如果发送的用户不在线，则缓存起来，登录的时候，进行发送,key-value:userId-List<MessageObj>
    private static ConcurrentHashMap<String, List<MessageObj>> cacheUserMessageMap = new ConcurrentHashMap<>();

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, TextWebSocketFrame textWebSocketFrame) throws Exception {
        //读取客户端发送的消息
        String text = textWebSocketFrame.text();
        MessageObj messageObj = JsonUtils.json2obj(text, MessageObj.class);
        System.out.println("接收到客户端【" + messageObj.getFromUserId() + "】发送的消息:" + messageObj.getMessage());
        ChannelId channelId = channelHandlerContext.channel().id();
        if (!channelIdMap.containsKey(channelId)) {
            channelIdMap.put(channelId, channelHandlerContext);
        }
        if (!channelUserNameMap.containsKey(messageObj.getFromUserId())) {
//            String userFromToStr = messageObj.getFromUserId() + "->" + (StringUtils.isBlank(messageObj.getToUserId()) ? messageObj.getToGroupId() : messageObj.getToUserId());
            channelUserNameMap.put(messageObj.getFromUserId(), channelHandlerContext.channel());
        }
        //判断当前发送消息的是单独聊天还是群发，此处距离单独发送,群发根据群的id，在数据库/缓存中查找群中的所有用户，在来发送，没有登录的则缓存数据
        //通过发送给的用户找到用户的通道并发送内容；
        if ("2".equals(messageObj.getType())) {
            messageObj.setMessage(messageObj.getFromUserId() + "：" + messageObj.getMessage());
            if (channelUserNameMap.containsKey(messageObj.getToUserId())) {
                Channel channel = channelUserNameMap.get(messageObj.getToUserId());
                channel.writeAndFlush(new TextWebSocketFrame(JsonUtils.obj2json(messageObj)));
            } else {
                System.out.println("发送给客户端【" + messageObj.getToUserId() + "】不在线");
                //发送给客户端不存在，则把消息缓存起来；账号登录的时候，进行发送
                List<MessageObj> messageList = null;
                if (cacheUserMessageMap.containsKey(messageObj.getToUserId())) {
                    messageList = cacheUserMessageMap.get(messageObj.getToUserId());
                } else {
                    messageList = Lists.newArrayList();
                }
                messageList.add(messageObj);
                cacheUserMessageMap.put(messageObj.getToUserId(), messageList);
            }
        } else if ("1".equals(messageObj.getType())) {
            //登录，发送给自己登录成功
            messageObj.setMessage(messageObj.getFromUserId() + "：登录成功");
            System.out.println("登录返回的信息:" + JsonUtils.obj2json(messageObj));
            channelHandlerContext.channel().writeAndFlush(new TextWebSocketFrame(JsonUtils.obj2json(messageObj)));
            //如果登录的当前账号，在缓存待发送消息中存在消息，则发送，并删除缓存中的数据。
            if (cacheUserMessageMap.containsKey(messageObj.getFromUserId())) {
                List<MessageObj> messageList = cacheUserMessageMap.get(messageObj.getFromUserId());
                System.out.println("客户端账户【"+messageObj.getFromUserId()+"】待接收的数据条数："+messageList.size());
                messageList.forEach(n -> {
                    channelHandlerContext.channel().writeAndFlush(new TextWebSocketFrame(JsonUtils.obj2json(n)));
                });
                cacheUserMessageMap.remove(messageObj.getFromUserId());
            }
        }
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        //新用户加入
        System.out.println("欢迎新用户加入");
        channelIdMap.put(ctx.channel().id(), ctx);
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        //用户离开
        ChannelId channelId = ctx.channel().id();
        String userName = getUser(true, ctx);
        if (channelIdMap.containsKey(channelId)) {
            channelIdMap.remove(channelId);
        }
        System.out.println("客户端用户【" + userName + "】离开");
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        //用户在线
        ChannelId channelId = ctx.channel().id();
        String userName = getUser(false, ctx);

        System.out.println("客户端用户【" + userName + "】在线");
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        //用户掉线
        ChannelId channelId = ctx.channel().id();
        String userName = getUser(false, ctx);

        System.out.println("客户端用户【" + userName + "】掉线");
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        //用户异常
        ChannelId channelId = ctx.channel().id();
        String userName = getUser(false, ctx);
        System.out.println("客户端用户【" + userName + "】异常：" + cause.getMessage());
    }

    /**
     * 获取用户，并根据isDeleteMap判断是否删除用户的通道
     * @param isDeleteMap 是否删除用户通道
     * @param ctx ChannelHandlerContext
     * @return String
     */
    private String getUser(boolean isDeleteMap, ChannelHandlerContext ctx) {
        String userName = null;
        if (channelUserNameMap.containsValue(ctx.channel())) {
            Iterator<Map.Entry<String, Channel>> iterator = channelUserNameMap.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, Channel> next = iterator.next();
                if (next.getValue().equals(ctx.channel())) {
                    userName = next.getKey();
                    if (isDeleteMap) {
                        iterator.remove();
                    }
                    break;
                }
            }
        }
        return userName;
    }
}
