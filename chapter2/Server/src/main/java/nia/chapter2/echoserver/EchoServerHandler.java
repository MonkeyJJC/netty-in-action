package nia.chapter2.echoserver;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.CharsetUtil;

/**
 * Listing 2.1 EchoServerHandler
 *
 * @author <a href="mailto:norman.maurer@gmail.com">Norman Maurer</a>
 */
/** 标示一个 ChannelHandler 可以被多 个 Channel 安全地 共享 */
@Sharable
public class EchoServerHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        ByteBuf in = (ByteBuf) msg;
        System.out.println(
                "Server received: " + in.toString(CharsetUtil.UTF_8));
        // 将接收到的消息写给发送者，而不冲刷出站消息
        // 会从当前的hander往前找第一个outbound来执行。记住一定要将OutBoundHandler先添加进ChannelPipeline
        // 如果我们是使用channel().write()方法。可以不用考虑outbound和inbound的添加顺序。每次都会从tail往前找第一个是outbound的handler来执行
        // https://juejin.cn/post/6844904013679296526
        // 不是立即写出去，就是写到outbound ，然后调用下flush就是真写出去了
        ctx.write(in);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx)
            throws Exception {
        // 注意，write方法不会将消息写入底层的socket，而只会将它放入队列中，要将它写入socket，需要调用flush()或者writeAndFlush()
        //  将未决消息冲刷到远程节点（真正将我们的消息发送出去），并且关闭该Channel
        ctx.writeAndFlush(Unpooled.EMPTY_BUFFER)
                // 对ChannelFuture新增listener，即监听上述ChannelFuture，当执行完成时，触发注册的Listener
                .addListener(ChannelFutureListener.CLOSE);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx,
        Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
