package nia.chapter6;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;

/**
 * Listing 6.1 Releasing message resources
 * 重新ChannelInboundHandlerAdapter的channelRead方法进行Channel数据的读取，需要进行内存释放
 * @author <a href="mailto:norman.maurer@gmail.com">Norman Maurer</a>
 */
@Sharable
public class DiscardHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        // 显式地释放与池化ByteBuf实例相关的内存，丢弃已接收的消息
        ReferenceCountUtil.release(msg);
    }

}

