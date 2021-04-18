package nia.chapter12;

import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.stream.ChunkedNioFile;

import java.io.File;
import java.io.RandomAccessFile;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * Listing 12.1 HTTPRequestHandler
 * 用于处理HTTP请求
 * https://www.w3cschool.cn/essential_netty_in_action/essential_netty_in_action-7kw128e0.html
 * @author <a href="mailto:norman.maurer@gmail.com">Norman Maurer</a>
 */
// 1 用于处理FullHttpRequest
public class HttpRequestHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
    private final String wsUri;
    private static final File INDEX;

    static {
        URL location = HttpRequestHandler.class
             .getProtectionDomain()
             .getCodeSource().getLocation();
        try {
            String path = location.toURI() + "index.html";
            path = !path.contains("file:") ? path : path.substring(5);
            INDEX = new File(path);
        } catch (URISyntaxException e) {
            throw new IllegalStateException(
                 "Unable to locate index.html", e);
        }
    }

    public HttpRequestHandler(String wsUri) {
        this.wsUri = wsUri;
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
        if (wsUri.equalsIgnoreCase(request.getUri())) {
            // 2 如果请求是一次升级了的 WebSocket 请求，则递增引用计数器（retain）并且将它传递给在 ChannelPipeline 中的下个 ChannelInboundHandler
            // 对于当前demo的ChannelPipeline，即传给WebSocketServerProtocolHandler
            // retain() 的调用是必要的，因为 channelRead() 完成后，它会调用 FullHttpRequest 上的 release() 来释放其资源。
            ctx.fireChannelRead(request.retain());
        } else {
            // HTTP 100-Continue 信息型状态响应码表示目前为止一切正常, 客户端应该继续请求, 如果已完成请求则忽略
            // 为了让服务器检查请求的首部, 客户端必须在发送请求实体前, 在初始化请求中发送 Expect: 100-continue 首部并接收100Continue响应状态码
            // 一般post数据情况才使用，100-continue的目的：https://blog.csdn.net/skh2015java/article/details/88723028
            if (HttpHeaders.is100ContinueExpected(request)) {
                // 3 处理符合 HTTP 1.1的 "100 Continue" 请求
                send100Continue(ctx);
            }
            // 4 读取 index.html资源文件
            RandomAccessFile file = new RandomAccessFile(INDEX, "r");
            HttpResponse response = new DefaultHttpResponse(request.getProtocolVersion(), HttpResponseStatus.OK);
            response.headers().set(HttpHeaders.Names.CONTENT_TYPE, "text/html; charset=UTF-8");
            boolean keepAlive = HttpHeaders.isKeepAlive(request);
            // 5 判断 keepalive 是否在请求头里面，http keep-alive是应用层机制，http server端的一种长连接机制
            if (keepAlive) {
                response.headers().set(HttpHeaders.Names.CONTENT_LENGTH, file.length());
                response.headers().set( HttpHeaders.Names.CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
            }
            // 6 写 HttpResponse 到客户端， 注意write时还在buffer中，需要flush才真正写
            ctx.write(response);
            // 7 写 index.html 到客户端，根据 ChannelPipeline 中是否有 SslHandler 来决定使用 DefaultFileRegion 还是 ChunkedNioFile
            // 如果传输过程既没有要求加密也没有要求压缩，那么把 index.html 的内容存储在一个 DefaultFileRegion 里就可以达到最好的效率。
            // 这将利用零拷贝来执行传输。出于这个原因，我们要检查 ChannelPipeline 中是否有一个 SslHandler。如果是的话，我们就使用 ChunkedNioFile。
            if (ctx.pipeline().get(SslHandler.class) == null) {
                // 使用零拷贝特性来高效地传输文件，使用FileRegion传输文件的内容，原因：Netty中如何写大型数据(由于NIO的零拷贝特性，这种特性消除了将文件的内容从文件系统移动到网络栈的复制过程)- https://www.cnblogs.com/shamo89/p/8600833.html
                // 大型数据避免导致OutOfMemoryError的风险
                ctx.write(new DefaultFileRegion(file.getChannel(), 0, file.length()));
            } else {
                ctx.write(new ChunkedNioFile(file.getChannel()));
            }
            // 8 写并刷新 LastHttpContent 到客户端，标记响应完成，写入结束符LastHttpContent.EMPTY_LAST_CONTENT
            // 这里我们调用 writeAndFlush() 来刷新所有以前写的信息
            ChannelFuture future = ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
            if (!keepAlive) {
                // 9 请求头中不包含 keepalive，当写完成时，关闭 Channel
                // netty整体的异步及响应思想，所以是对future绑定监听器进行操作
                future.addListener(ChannelFutureListener.CLOSE);
            }
        }
    }

    private static void send100Continue(ChannelHandlerContext ctx) {
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.CONTINUE);
        ctx.writeAndFlush(response);
    }
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
