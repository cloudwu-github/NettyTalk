package com.sofn.netty.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Data
public class NettyConfig {
    @Value("${netty.url}")
    private String nettyIp;
    @Value("${netty.port}")
    private int nettyPort;
}
