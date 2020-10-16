package com.sofn.netty.websocket;

import com.sofn.common.utils.JsonUtils;
import com.sofn.netty.handle.NettyServerHandler;
import com.sofn.netty.init.NettyServerChannelInitializer;
import com.sofn.netty.model.SendMessage;
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
public class NettyWebSocketServer {
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
                    .childHandler(new NettyWebSocketChannelInitializer());////编码解码
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
}
