package com.sofn.netty.init;

import com.sofn.netty.handle.NettyServerHandler;
import com.sofn.netty.model.MessageBase;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.CharsetUtil;

/**
 * 服务端初始化，客户端与服务器端连接一旦创建，这个类中方法就会被回调，设置出站编码器和入站解码器
 */
public class NettyServerChannelInitializer extends ChannelInitializer<SocketChannel> {

    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        ChannelPipeline pipeline = socketChannel.pipeline();
        //解决tcp粘包的问题，LineBasedFrameDecoder；
//        pipeline.addLast("lineBasedFrameDecoder",new LineBasedFrameDecoder(1024));
//        pipeline.addLast("decode", new StringDecoder(CharsetUtil.UTF_8));
//        pipeline.addLast("encode",new StringEncoder(CharsetUtil.UTF_8));
        //使用protobuf编解码框架的配置
        //解决tcp粘包的问题：ProtobufVarint32FrameDecoder
        pipeline.addLast(new ProtobufVarint32FrameDecoder());
        pipeline.addLast(new ProtobufDecoder(MessageBase.Message.getDefaultInstance()));
        pipeline.addLast(new ProtobufVarint32LengthFieldPrepender());
        pipeline.addLast(new ProtobufEncoder());
        pipeline.addLast(new NettyServerHandler());
    }
}
