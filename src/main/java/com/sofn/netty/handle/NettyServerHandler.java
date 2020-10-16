package com.sofn.netty.handle;

import com.sofn.netty.model.MessageBase;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelId;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ObjectUtils;

import java.net.InetSocketAddress;
import java.util.concurrent.ConcurrentHashMap;

/**
 * netty服务处理类
 */
@Slf4j
public class NettyServerHandler extends ChannelInboundHandlerAdapter {

    //管理一个全局map，保存连接进服务端的通道数量
    public static ConcurrentHashMap<ChannelId,ChannelHandlerContext> CHANNEL_MAP=new  ConcurrentHashMap<>();

    /**
     * 有客户端连接服务器会触发此函数
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        InetSocketAddress socketAddress = (InetSocketAddress) ctx.channel().remoteAddress();
        String clientIp = socketAddress.getAddress().getHostAddress();
        int clientPort = socketAddress.getPort();
        ChannelId channelId = ctx.channel().id();
        StringBuilder stringBuilder=null;
        if(CHANNEL_MAP.containsKey(channelId)){
            //记录日志，连接成功
            stringBuilder=new StringBuilder()
                    .append("客户端【")
                    .append(channelId)
                    .append("】是连接状态，当前连接客户端数：")
                    .append(CHANNEL_MAP.size());
            log.info(stringBuilder.toString());
        }else{
            //保存客户端连接信息
            CHANNEL_MAP.put(channelId,ctx);
            stringBuilder=new StringBuilder()
                    .append("客户端【")
                    .append(channelId)
                    .append("】连接netty服务端成功，ip：")
                    .append(clientIp)
                    .append("，端口号：")
                    .append(clientPort)
                    .append("当前连接客户端数：")
                    .append(CHANNEL_MAP.size());
            log.info(stringBuilder.toString());
        }
    }

    /**
     * 有客户端终止连接服务器会触发此函数
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        InetSocketAddress socketAddress = (InetSocketAddress) ctx.channel().remoteAddress();
        String clientIp = socketAddress.getAddress().getHostAddress();
        int clientPort = socketAddress.getPort();
        ChannelId channelId = ctx.channel().id();
        if(CHANNEL_MAP.containsKey(channelId)){
            CHANNEL_MAP.remove(channelId);
            StringBuilder sb=new StringBuilder()
                    .append("客户端【")
                    .append(channelId)
                    .append("】退出netty服务端成功，ip：")
                    .append(clientIp)
                    .append("，端口号：")
                    .append(clientPort)
                    .append("当前连接客户端数：")
                    .append(CHANNEL_MAP.size());
            System.out.println(sb.toString());
        }
    }

    //有客户端发消息会触发此函数
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        //读取客服端发送到服务端的消息；
        //判断发送消息的频道id是否存在
        ChannelId channelId = ctx.channel().id();
        if(!CHANNEL_MAP.containsKey(channelId)){
            log.error("接收到发送的消息通道id，不存在-"+channelId);
            return;
        }
        if(ObjectUtils.isEmpty(msg)){
            log.error("发送的是空消息-"+msg);
        }
        ctx.writeAndFlush(msg);
        MessageBase.Message message= (MessageBase.Message)msg;
        System.out.println("服务端接收到的消息："+msg);
        System.out.println("服务端接收到的消息的内容："+message.getContent());
    }

    //发生异常会触发此函数
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ChannelId id = ctx.channel().id();
        ctx.close();
        StringBuilder sb=new StringBuilder()
                .append("客户端【")
                .append(id)
                .append("】发生异常：")
                .append(cause.getMessage());
        log.error(sb.toString());
    }
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        ctx.fireUserEventTriggered(evt);
    }

}
