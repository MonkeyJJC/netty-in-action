package nia.chapter2.echoserver;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.net.InetSocketAddress;

/**
 * Listing 2.2 EchoServer class
 *
 * @author <a href="mailto:norman.maurer@gmail.com">Norman Maurer</a>
 */
public class EchoServer {
    private final int port;

    public EchoServer(int port) {
        this.port = port;
    }

    public static void main(String[] args)
        throws Exception {
        if (args.length != 1) {
            System.err.println("Usage: " + EchoServer.class.getSimpleName() +
                " <port>"
            );
            return;
        }
        int port = Integer.parseInt(args[0]);
        // 设置端口值port，调用start()初始化方法
        new EchoServer(port).start();
    }

    public void start() throws Exception {
        // 创建ChannelHandler对象，用于跟Channel绑定
        final EchoServerHandler serverHandler = new EchoServerHandler();
        // 参考 https://www.cnblogs.com/duanxz/p/3724395.html
        // NioEventLoopGroup是一个Schedule类型的线程池，线程池中的线程用数组存放， EventLoopGroup(其实是MultithreadEventExecutorGroup) 内部维护一个类型为 EventExecutor children 数组, 其大小是 nThreads, 这样就构成了一个线程池，线程池大小通过 在实例化 NioEventLoopGroup 时, 如果指定线程池大小, 则 nThreads 就是指定的值, 反之是处理器核心数 * 2
        // NioEventLoop两大功能：1.是作为 IO 线程, 执行与 Channel 相关的 IO 操作, 包括 调用 select 等待就绪的 IO 事件、读写数据与数据的处理等；2.为任务队列执行任务， 任务可以分为2类：普通task与定时任务执行schedule()方法
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(group)
                    // 创建并分配一个 NioEventLoopGroup 实例以进行事件的处理，如接受新连接以及读/写数据
                    // 指定channel类型,channel方法的调用其实就是实例化了一个用于生成此channel类型对象的工厂对象。 并且在bind调用后，会调用此工厂对象来生成一个新channel。
                    // 通过ServerBootstrap.channel方法的调用生成channelFactory对象
                    // 1.NioServerSocketChannel对象内部绑定了Java NIO创建的ServerSocketChannel对象；
                    // 2.Netty中，每个channel都有一个unsafe对象，此对象封装了Java NIO底层channel的操作细节；
                    // 3.Netty中，每个channel都有一个pipeline对象，此对象就是一个双向链表；
                    .channel(NioServerSocketChannel.class)
                    // 使用指定的 端口设置套 接字地址
                    .localAddress(new InetSocketAddress(port))
                    //  添加一个Handler到子Channel的ChannelPipeline
                    // ChannelInitializer:当一个新的连接被接受时，一个新的子 Channel 将会被创建，而ChannelInitializer将会把一个你的EchoServerHandler的实例添加到该Channel的ChannelPipeline中，用于初始化每一个新的Channel
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            //  EchoServerHandler被标注为@Shareable，所以我们可以总是使用同样的实例
                            ch.pipeline().addLast(serverHandler);
                        }
                    });
            // 异步地绑定服务器; 调用sync()方法阻塞,等待直到绑定完成
            ChannelFuture f = b.bind().sync();
            System.out.println(EchoServer.class.getName() +
                " started and listening for connections on " + f.channel().localAddress());
            // 获取Channel的CloseFuture，并且阻塞当前线程直到它完成 https://www.javadoop.com/post/netty-part-3
            f.channel().closeFuture().sync();
        } finally {
            //  关闭 EventLoopGroup， 释放所有的资源
            group.shutdownGracefully().sync();
        }
    }
}
