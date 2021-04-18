package nia.chapter6;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.netty.util.ReferenceCountUtil;

/**
 * Listing 6.4 Discarding and releasing outbound data
 * outbound中flush方法：当请求通过Channel将入队数据冲刷到远程节点时被调用
 * ChannelOutboundHandler中的大部分方法都需要一个ChannelPromise参数，以便在操作完成时得到通知。
 * ChannelPromise是ChannelFuture的一个 子类，其定义了一些可写的方法，如setSuccess()和setFailure()，从而使ChannelFuture不可变。当一个Promise被完成之后，其对应的Future的值便不能再进行任何修改了。
 * @author <a href="mailto:norman.maurer@gmail.com">Norman Maurer</a>
 */
@Sharable
public class DiscardOutboundHandler extends ChannelOutboundHandlerAdapter {
    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) {
        // 消息出站，丢弃并释放出站消息
        ReferenceCountUtil.release(msg);
        // 通知ChannelPromise数据已经被处理，一定要setSuccess通知，否则可能出现ChannelFutureListener收不到某个消息已经被处理了的通知情况
        promise.setSuccess();
    }
}

