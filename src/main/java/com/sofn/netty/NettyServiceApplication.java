package com.sofn.netty;

import com.sofn.netty.config.NettyConfig;
import com.sofn.netty.server.NettyServer;
import com.sofn.netty.websocket.NettyWebSocketServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.net.InetSocketAddress;

@SpringBootApplication
public class NettyServiceApplication implements CommandLineRunner {
    @Autowired
    private NettyConfig nettyConfig;
//    @Autowired
//    private NettyServer nettyServer;
    @Autowired
    private NettyWebSocketServer nettyWebSocketServer;

    public static void main(String[] args) {
        SpringApplication.run(NettyServiceApplication.class);
    }

    @Override
    public void run(String... args) throws Exception {
        InetSocketAddress inetSocketAddress=new InetSocketAddress(nettyConfig.getNettyIp(),nettyConfig.getNettyPort());
//        nettyServer.start(inetSocketAddress);
        nettyWebSocketServer.start(inetSocketAddress);
    }
}
