package nia.chapter2.echoclient;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.CharsetUtil;

/**
 * Listing 2.3 ChannelHandler for the client
 *
 * @author <a href="mailto:norman.maurer@gmail.com">Norman Maurer</a>
 */
@Sharable
public class EchoClientHandler extends SimpleChannelInboundHandler<ByteBuf> {
    /**
     * 在到服务器的连接已经建立之后将被调用
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        // 发送一条消息
        ctx.writeAndFlush(Unpooled.copiedBuffer("Netty rocks!",
                CharsetUtil.UTF_8));
    }

    /**
     * 当从服务器接收到一条消息时被调用
     * 由服务器发送的消息可能会被分块接收。也就是说，如果服务器发送了 5 字节，那么不能保证这 5 字节会被一次性接收。
     * 即使是对于这么少量的数据，channelRead0()方法也可能会被调用两次，第一次使用一个持有 3 字节的ByteBuf(Netty的字节容器)，
     * 第二次使用一个 持有 2 字节的 ByteBuf。
     * 作为一个面向流的协议，TCP 保证了字节数组将会按照服务器发送它们的顺序被接收。
     */
    @Override
    public void channelRead0(ChannelHandlerContext ctx, ByteBuf in) {
        System.out.println(
                "Client received: " + in.toString(CharsetUtil.UTF_8));
        // channelRead0执行结束，SimpleChannelInboundHandler 负责释放指向保存该消息的ByteBuf的内存引用
    }

    /**
     * 在处理过程中引发异常时被调用
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx,
        Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
