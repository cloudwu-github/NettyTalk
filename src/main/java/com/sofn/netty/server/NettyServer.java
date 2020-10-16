package com.sofn.netty.server;

import com.alibaba.fastjson.serializer.PropertyFilter;
import com.sofn.common.utils.JsonUtils;
import com.sofn.netty.model.SendMessage;
import com.sofn.netty.handle.NettyServerHandler;
import com.sofn.netty.init.NettyServerChannelInitializer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelId;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;
import java.util.concurrent.ConcurrentHashMap;

/**
 * netty 服务启动类
 */
@Slf4j
@Component
public class NettyServer {
    public void start(InetSocketAddress address) {
        ////创建主线程组，接收请求
        NioEventLoopGroup bossGroup = new NioEventLoopGroup(1);
        //创建从线程组，处理主线程组分配下来的io操作
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            //创建netty服务器
            ServerBootstrap bootstrap=new ServerBootstrap()
                    .group(bossGroup,workerGroup) //设置主从线程组
                    .channel(NioServerSocketChannel.class) //设置通道
                    .localAddress(address)
                    .childHandler(new NettyServerChannelInitializer())////编码解码
                    .option(ChannelOption.SO_BACKLOG,128) //服务端接受连接的队列长度，如果队列已满，客户端连接将被拒绝
                    .childOption(ChannelOption.SO_KEEPALIVE,true);//保持长连接，2小时无数据激活心跳机制
            ChannelFuture future = bootstrap.bind(address).sync();
            //关闭channel和块，直到它被关闭
            future.channel().closeFuture().sync();
        } catch (Exception ex) {
            ex.printStackTrace();
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
        System.out.println("netty服务端启动成功！");
    }

    /**
     * 服务端向客户端发送消息
     * @param sendMessage 消息对象
     */
    public <T> void sendMessage(SendMessage<T> sendMessage){
        ConcurrentHashMap<ChannelId, ChannelHandlerContext> channelMap = NettyServerHandler.CHANNEL_MAP;
        if(channelMap!=null){
            StringBuilder sb=new StringBuilder();
            String messageStr;
            for (ChannelId channelId :channelMap.keySet()){
                //向每一个客户端发送一个消息
                ChannelHandlerContext channelHandlerContext = channelMap.get(channelId);
                messageStr= JsonUtils.obj2json(sendMessage);
                channelHandlerContext.writeAndFlush(JsonUtils.obj2json(sendMessage));
                sb.append("发送的客户端:")
                        .append(channelId)
                        .append("，发送的消息：")
                        .append(messageStr);
                System.out.println(sb.toString());
            }
        }
    }
}
