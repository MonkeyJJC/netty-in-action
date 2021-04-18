package nia.chapter4;

import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.Charset;

/**
 * Listing 4.1 Blocking networking without Netty
 * 未使用netty的OIO通信
 * @author <a href="mailto:norman.maurer@gmail.com">Norman Maurer</a>
 */
public class PlainOioServer {
    public void serve(int port) throws IOException {
        // 将服务器指定到指定端口
        final ServerSocket socket = new ServerSocket(port);
        try {
            for(;;) {
                // 接受连接，监听套接字建立的连接，阻塞直至连接建立(可以处理中等数量的并发客户端，并发过高时需要异步)
                final Socket clientSocket = socket.accept();
                System.out.println(
                        "Accepted connection from " + clientSocket);
                // 用线程池可以有多个客户端连接，但是非常消耗性能，每一个客户都需要一个线程提供独立服务
                // 新建一个线程处理该连接
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        OutputStream out;
                        try {
                            out = clientSocket.getOutputStream();
                            // 将消息写到客户端
                            out.write("Hi!\r\n".getBytes(
                                    Charset.forName("UTF-8")));
                            out.flush();
                            // 关闭连接
                            clientSocket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        } finally {
                            try {
                                clientSocket.close();
                            } catch (IOException ex) {
                                // ignore on close
                            }
                        }
                    }
                    // 启动线程
                }).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
