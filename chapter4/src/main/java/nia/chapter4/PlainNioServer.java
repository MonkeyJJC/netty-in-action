package nia.chapter4;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

/**
 * Listing 4.2 Asynchronous networking without Netty
 * 未使用netty的NIO通信
 * @author <a href="mailto:norman.maurer@gmail.com">Norman Maurer</a>
 */
public class PlainNioServer {
    public void serve(int port) throws IOException {
        ServerSocketChannel serverChannel = ServerSocketChannel.open();
        serverChannel.configureBlocking(false);
        ServerSocket ss = serverChannel.socket();
        InetSocketAddress address = new InetSocketAddress(port);
        // 将服务绑定到选定的端口
        ss.bind(address);
        // 打开Selector处理channel
        Selector selector = Selector.open();
                // 将channel注册到selector
                serverChannel.register(selector, SelectionKey.OP_ACCEPT);
                final ByteBuffer msg = ByteBuffer.wrap("Hi!\r\n".getBytes());
                for (;;){
                    try {
                        // 等待需要处理的新事件，阻塞将一直持续到下一个传入事件
                        selector.select();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                        //handle exception
                        break;
                    }
                    // 获取所有接收事件的selected-key实例
                    Set<SelectionKey> readyKeys = selector.selectedKeys();
                    Iterator<SelectionKey> iterator = readyKeys.iterator();
                    while (iterator.hasNext()) {
                        SelectionKey key = iterator.next();
                        iterator.remove();
                        try {
                            // 检查事件是否是一个新的已经就绪可以被接受的连接
                            if (key.isAcceptable()) {
                                ServerSocketChannel server =
                                        (ServerSocketChannel) key.channel();
                                // 接受客户端
                                SocketChannel client = server.accept();
                                client.configureBlocking(false);
                                // 将客户端channel注册到选择器
                                client.register(selector, SelectionKey.OP_WRITE |
                                        SelectionKey.OP_READ, msg.duplicate());
                                System.out.println(
                                        "Accepted connection from " + client);
                            }
                            // 检查channel是否就绪可以写数据
                            if (key.isWritable()) {
                                // SocketChannel是一个连接到TCP网络套接字的通道
                                SocketChannel client =
                                        (SocketChannel) key.channel();
                                ByteBuffer buffer =
                                        (ByteBuffer) key.attachment();
                                while (buffer.hasRemaining()) {
                                    // 将数据写到已经连接的客户端
                                    // SocketChannel.write()方法的调用是在一个while循环中的。Write()方法无法保证能写多少字节到SocketChannel。所以，我们重复调用write()直到Buffer没有要写的字节为止
                                    if (client.write(buffer) == 0) {
                                        break;
                                    }
                                }
                                // 客户端client关闭连接，即客户端断开
                                client.close();
                            }
                        } catch (IOException ex) {
                            key.cancel();
                            try {
                                key.channel().close();
                            } catch (IOException cex) {
                                // ignore on close
                            }
                        }
            }
        }
    }
}

