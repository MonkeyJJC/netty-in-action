package nia.chapter6;

import io.netty.channel.*;

/**
 * Listing 6.14 Adding a ChannelFutureListener to a ChannelPromise
 * 几乎所有的ChannelOutboundHandler上的方法都会传入一个ChannelPromise的实例,用于事件监听通知
 * @author <a href="mailto:norman.maurer@gmail.com">Norman Maurer</a>
 */
public class OutboundExceptionHandler extends ChannelOutboundHandlerAdapter {
    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) {
        // 注册到ChannelFuture的ChannelFutureListener将在操作完成时被通知该操作是成功了还是出错了。
        promise.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture f) {
                if (!f.isSuccess()) {
                    f.cause().printStackTrace();
                    f.channel().close();
                }
            }
        });
    }
}
