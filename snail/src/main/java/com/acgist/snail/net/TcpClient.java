package com.acgist.snail.net;

import java.io.IOException;
import java.net.StandardSocketOptions;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.acgist.snail.context.SystemThreadContext;
import com.acgist.snail.logger.Logger;
import com.acgist.snail.logger.LoggerFactory;
import com.acgist.snail.utils.IoUtils;
import com.acgist.snail.utils.NetUtils;

/**
 * TCP客户端
 * 
 * @param <T> TCP消息代理类型
 * 
 * @author acgist
 */
public abstract class TcpClient<T extends TcpMessageHandler> extends Client<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(TcpClient.class);
    
    /**
     * 客户端线程池
     */
    private static final AsynchronousChannelGroup GROUP;
    
    static {
        AsynchronousChannelGroup group = null;
        try {
            final ExecutorService executor = SystemThreadContext.newCacheExecutor(0, 60L, SystemThreadContext.SNAIL_THREAD_TCP_CLIENT);
            group = AsynchronousChannelGroup.withThreadPool(executor);
        } catch (IOException e) {
            LOGGER.error("启动TCP Client线程池异常", e);
        }
        GROUP = group;
    }
    
    /**
     * 超时时间（秒）
     */
    private final int timeout;
    
    /**
     * TCP客户端
     * 
     * @param name    客户端名称
     * @param timeout 超时时间（秒）
     * @param handler 消息代理
     */
    protected TcpClient(String name, int timeout, T handler) {
        super(name, handler);
        this.timeout = timeout;
    }
    
    /**
     * 连接服务端
     * 
     * @return 连接状态
     * 
     * @see #connect(String, int)
     */
    public abstract boolean connect();
    
    /**
     * 连接服务端
     * 
     * @param host 服务端地址
     * @param port 服务端端口
     * 
     * @return 连接状态
     */
    protected boolean connect(final String host, final int port) {
        boolean success = true;
        AsynchronousSocketChannel channel = null;
        try {
            channel = AsynchronousSocketChannel.open(GROUP);
//          channel.setOption(StandardSocketOptions.TCP_NODELAY, true);
            channel.setOption(StandardSocketOptions.SO_REUSEADDR, true);
            channel.setOption(StandardSocketOptions.SO_KEEPALIVE, true);
            final Future<Void> future = channel.connect(NetUtils.buildSocketAddress(host, port));
            future.get(this.timeout, TimeUnit.SECONDS);
            this.handler.handle(channel);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOGGER.error("TCP客户端连接异常：{} - {}", host, port, e);
            success = false;
        } catch (IOException | ExecutionException | TimeoutException e) {
            LOGGER.error("TCP客户端连接异常：{} - {}", host, port, e);
            success = false;
        } finally {
            if(success) {
                LOGGER.debug("TCP连接成功：{} - {}", host, port);
            } else {
                IoUtils.close(channel);
                this.close();
            }
        }
        return success;
    }
    
    @Override
    public void close() {
        LOGGER.debug("关闭TCP Client：{}", this.name);
        super.close();
    }

    /**
     * 关闭TCP Client线程池
     */
    public static final void shutdown() {
        LOGGER.debug("关闭TCP Client线程池");
        SystemThreadContext.shutdown(GROUP);
    }

}
