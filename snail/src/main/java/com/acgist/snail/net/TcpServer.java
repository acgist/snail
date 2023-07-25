package com.acgist.snail.net;

import java.io.IOException;
import java.net.StandardSocketOptions;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.util.concurrent.ExecutorService;

import com.acgist.snail.context.SystemThreadContext;
import com.acgist.snail.logger.Logger;
import com.acgist.snail.logger.LoggerFactory;
import com.acgist.snail.utils.IoUtils;
import com.acgist.snail.utils.NetUtils;

/**
 * TCP服务端
 * 
 * @param <T> TCP消息代理类型
 * 
 * @author acgist
 */
public abstract class TcpServer<T extends TcpMessageHandler> extends Server<AsynchronousServerSocketChannel> {

    private static final Logger LOGGER = LoggerFactory.getLogger(TcpServer.class);
    
    /**
     * 服务端线程池
     */
    private static final AsynchronousChannelGroup GROUP;
    
    static {
        AsynchronousChannelGroup group = null;
        try {
            final ExecutorService executor = SystemThreadContext.newCacheExecutor(0, 60L, SystemThreadContext.SNAIL_THREAD_TCP_SERVER);
            group = AsynchronousChannelGroup.withThreadPool(executor);
        } catch (IOException e) {
            LOGGER.error("启动TCP Server线程池异常");
        }
        GROUP = group;
    }
    
    /**
     * 消息代理类型
     */
    private final Class<T> clazz;
    
    /**
     * TCP服务端
     * 
     * @param name  服务端名称
     * @param clazz 消息代理类型
     */
    protected TcpServer(String name, Class<T> clazz) {
        super(name);
        this.clazz = clazz;
    }

    /**
     * 开启监听
     * 
     * @return 是否监听成功
     */
    public abstract boolean listen();
    
    /**
     * 开启监听
     * 
     * @param port 端口
     * 
     * @return 是否监听成功
     */
    protected boolean listen(int port) {
        return this.listen(ADDR_LOCAL, port, ADDR_UNREUSE);
    }
    
    @Override
    protected boolean listen(String host, int port, boolean reuse) {
        LOGGER.debug("启动TCP服务端：{} - {} - {} - {}", this.name, host, port, reuse);
        boolean success = true;
        try {
            this.channel = AsynchronousServerSocketChannel.open(GROUP);
            this.channel.setOption(StandardSocketOptions.SO_REUSEADDR, reuse);
            this.channel.bind(NetUtils.buildSocketAddress(host, port));
            this.channel.accept(this.channel, TcpAcceptHandler.newInstance(this.clazz));
        } catch (IOException e) {
            LOGGER.error("启动TCP服务端异常：{}", this.name, e);
            success = false;
        } finally {
            if(success) {
                LOGGER.debug("启动TCP服务端成功：{}", this.name);
            } else {
                this.close();
            }
        }
        return success;
    }
    
    /**
     * 关闭TCP Server
     */
    public void close() {
        LOGGER.debug("关闭TCP Server：{}", this.name);
        IoUtils.close(this.channel);
    }
    
    /**
     * 关闭TCP Server线程池
     */
    public static final void shutdown() {
        LOGGER.debug("关闭TCP Server线程池");
        SystemThreadContext.shutdown(GROUP);
    }
    
}
