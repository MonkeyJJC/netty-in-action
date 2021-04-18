package nia.chapter12;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.group.ChannelGroup;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.stream.ChunkedWriteHandler;

/**
 * Listing 12.3 Initializing the ChannelPipeline
 * HTTP/HTTPS协议切换到WebSocket时，会进行升级握手
 * 程序始终以HTTP/S作为开始，再进行升级
 * 升级时机：启动时或者请求了某个特定的URL之后
 * @author <a href="mailto:norman.maurer@gmail.com">Norman Maurer</a>
 */
public class ChatServerInitializer extends ChannelInitializer<Channel> {
    private final ChannelGroup group;

    public ChatServerInitializer(ChannelGroup group) {
        this.group = group;
    }

    @Override
    protected void initChannel(Channel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        // 功能：将字节解码为 HttpRequest、HttpContent 和 LastHttpContent。并将 HttpRequest、HttpContent 和 LastHttpContent 编码为字节。
        pipeline.addLast(new HttpServerCodec());
        // 功能：写入一个文件的内容（在文件需要进行加解密和压缩时使用）
        pipeline.addLast(new ChunkedWriteHandler());
        // 将一个 HttpMessage 和跟随它的多个 HttpContent 聚合 为单个 FullHttpRequest 或者 FullHttpResponse（取 决于它是被用来处理请求还是响应）。
        // 安装了这个之后， ChannelPipeline 中的下一个 ChannelHandler 将只会 收到完整的 HTTP 请求或响应
        pipeline.addLast(new HttpObjectAggregator(64 * 1024));
        // 功能：处理 FullHttpRequest（那些不发送到/ws URI 的请求）
        // 看源码channelRead0中有对wsUri的特判 netty添加WebSocket支持参
        // https://www.w3cschool.cn/essential_netty_in_action/essential_netty_in_action-7kw128e0.html
        pipeline.addLast(new HttpRequestHandler("/ws"));
        // 功能：如果/ws的uri被访问，那么将会升级协议为WebSocket
        // WebSocketServerProtocolHandler作为WebSocket协议的主要处理器
        // 看源码发现此方法经过简单的检查后将WebSocketHandshakeHandler添加到了本处理器之前，用于处理握手相关业务。channelRead方法会尝试接收一个FullHttpRequest对象，表示来自客户端的HTTP请求，随后服务器将会进行握手相关操作
        // 在确认握手成功后，channelRead将会调用两次fireUserEventTriggered，此方法将会触发自定义事件。其他（在此处理器之后）的处理器会触发userEventTriggered方法,通过监听自定义事件即可实现检查握手的HTTP请求
        pipeline.addLast(new WebSocketServerProtocolHandler("/ws"));
        // 处理 TextWebSocketFrame 和握手完成事件
        // WebSockets 在“帧”(Frame)里面来发送数据，其中每一个都代表了一个消息的一部分。一个完整的消息可以利用了多个帧。
        // WebSocket "Request for Comments" (RFC) 定义了六种不同的 frame，抽象类WebSocketFrame; Netty 给他们每个都提供了一个 POJO 实现 ,TextWebSocketFrame是其中之一
        // 协议升级等操作由WebSocketServerProtocolHandler完成，我们只需要自定义对应Frame的处理类
        pipeline.addLast(new TextWebSocketFrameHandler(group));
    }
}
