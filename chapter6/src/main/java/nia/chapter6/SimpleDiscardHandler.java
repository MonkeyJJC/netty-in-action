package nia.chapter6;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * Listing 6.2 Using SimpleChannelInboundHandler
 * SimpleChannelInboundHandler会自动释放资源，因此channelRead0中无需再release进行显式的资源释放(实际看下SimpleChannelInboundHandler的channelRead源码就很清晰了)
 * @author <a href="mailto:norman.maurer@gmail.com">Norman Maurer</a>
 */
@Sharable
public class SimpleDiscardHandler extends SimpleChannelInboundHandler<Object> {
    @Override
    public void channelRead0(ChannelHandlerContext ctx, Object msg) {
        // No need to do anything special
        // 不要存储任何消息msg的引用，因为这些引用会在channelRead中被释放，引用会失效
    }
}
