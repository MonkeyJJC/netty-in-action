package nia.chapter7;

import io.netty.channel.Channel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Listing 7.2 Scheduling a task with a ScheduledExecutorService
 *
 * Listing 7.3 Scheduling a task with EventLoop
 *
 * Listing 7.4 Scheduling a recurring task with EventLoop
 *
 * Listing 7.5 Canceling a task using ScheduledFuture
 *
 * @author <a href="mailto:norman.maurer@gmail.com">Norman Maurer</a>
 */
public class ScheduleExamples {
    private static final Channel CHANNEL_FROM_SOMEWHERE = new NioSocketChannel();

    /**
     * Listing 7.2 Scheduling a task with a ScheduledExecutorService
     * */
    public static void schedule() {
        // 创建一个其线程池具有10个线程的ScheduleExecutorService
        // 缺点：ScheduledExecutorService作为线程池管理的一部分，将会有额外的线程创建。如果有大量的任务被紧凑地调度，这将是一个瓶颈
        ScheduledExecutorService executor =
                Executors.newScheduledThreadPool(10);

        ScheduledFuture<?> future = executor.schedule(
            new Runnable() {
            @Override
            public void run() {
                System.out.println("Now it is 60 seconds later");
            }
            // 调度任务延迟60秒后执行，想周期循环执行使用scheduleAtFixedRate或scheduleAtFixedDelay
        }, 60, TimeUnit.SECONDS);
        //...调度任务执行完毕关闭Executor，释放资源
        executor.shutdown();
    }

    /**
     * Listing 7.3 Scheduling a task with EventLoop
     * 使用netty的EventLoop执行调度任务，EventLoop实际是扩展了ScheduledExecutorService
     * */
    public static void scheduleViaEventLoop() {
        Channel ch = CHANNEL_FROM_SOMEWHERE; // get reference from somewhere
        ScheduledFuture<?> future = ch.eventLoop().schedule(
            new Runnable() {
            @Override
            public void run() {
                System.out.println("60 seconds later");
            }
            // 延迟60秒执行，60秒后Runnable将由分配给Channel的EventLoop执行
        }, 60, TimeUnit.SECONDS);
    }

    /**
     * Listing 7.4 Scheduling a recurring task with EventLoop
     * */
    public static void scheduleFixedViaEventLoop() {
        Channel ch = CHANNEL_FROM_SOMEWHERE; // get reference from somewhere
        // 使用scheduleAtFixedRate可以每隔60秒执行一次
        // Runnable command, long initialDelay, long period, TimeUnit unit 设置初始等待间隔及周期
        ScheduledFuture<?> future = ch.eventLoop().scheduleAtFixedRate(
           new Runnable() {
           @Override
           public void run() {
               System.out.println("Run every 60 seconds");
               }
           }, 60, 60, TimeUnit.SECONDS);
    }

    /**
     * Listing 7.5 Canceling a task using ScheduledFuture
     * */
    public static void cancelingTaskUsingScheduledFuture(){
        Channel ch = CHANNEL_FROM_SOMEWHERE; // get reference from somewhere
        // 通过ScheduledFuture可以取消或者检查任务的执行状态
        ScheduledFuture<?> future = ch.eventLoop().scheduleAtFixedRate(
                new Runnable() {
                    @Override
                    public void run() {
                        System.out.println("Run every 60 seconds");
                    }
                }, 60, 60, TimeUnit.SECONDS);
        // Some other code that runs...
        boolean mayInterruptIfRunning = false;
        future.cancel(mayInterruptIfRunning);
    }

    public static void executeDemo() {
        Channel ch = CHANNEL_FROM_SOMEWHERE;
        // 通过Channel绑定的EventLoop执行任务
        ch.eventLoop().execute(() -> System.out.println("execute task"));
    }
}
