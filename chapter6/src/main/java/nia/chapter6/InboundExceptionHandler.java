package nia.chapter6;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * Listing 6.12 Basic inbound exception handling
 * 异常将会继续按照入站方向流动(就像所有的入站事件一样)，一般异常处理Handler通常位于ChannelPipeline的最后。这确保了所有的入站异常都总是会被处理，无论它们可能会发生在ChannelPipeline中的什么位置。
 * @author <a href="mailto:norman.maurer@gmail.com">Norman Maurer</a>
 */
public class InboundExceptionHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
