package com.acgist.snail.net;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ExecutorService;

import com.acgist.snail.config.SystemConfig;
import com.acgist.snail.context.SystemThreadContext;
import com.acgist.snail.logger.Logger;
import com.acgist.snail.logger.LoggerFactory;
import com.acgist.snail.utils.IoUtils;
import com.acgist.snail.utils.NetUtils;

/**
 * UDP服务端
 * 全部使用单例：初始化时立即开始监听（客户端和服务端使用同一个通道）
 * 
 * @param <T> UDP消息接收代理类型
 * 
 * @author acgist
 */
public abstract class UdpServer<T extends UdpAcceptHandler> extends Server<DatagramChannel> {

    private static final Logger LOGGER = LoggerFactory.getLogger(UdpServer.class);

    /**
     * 服务端线程池
     */
    private static final ExecutorService EXECUTOR;
    
    static {
        EXECUTOR = SystemThreadContext.newCacheExecutor(0, 60L, SystemThreadContext.SNAIL_THREAD_UDP_SERVER);
    }
    
    /**
     * 消息接收代理
     */
    private final T handler;
    /**
     * Selector：每个服务端独立
     * 
     * UDP：只有read/write
     * TCP：
     *  1. Server通道含有accept/read/write
     *  2. Client通道含有connect/read/write
     *  3. Server可以将accept和read/write分开注册多个Selector
     */
    private Selector selector;

    /**
     * UDP服务端
     * 
     * @param name    服务端名称
     * @param handler 消息接收代理
     * 
     * @see Server#PORT_AUTO
     * @see Server#ADDR_LOCAL
     * @see Server#ADDR_UNREUSE
     */
    protected UdpServer(String name, T handler) {
        this(ADDR_LOCAL, PORT_AUTO, ADDR_UNREUSE, name, handler);
    }
    
    /**
     * UDP服务端
     * 
     * @param port    端口
     * @param name    服务端名称
     * @param handler 消息接收代理
     * 
     * @see Server#ADDR_LOCAL
     * @see Server#ADDR_UNREUSE
     */
    protected UdpServer(int port, String name, T handler) {
        this(ADDR_LOCAL, port, ADDR_UNREUSE, name, handler);
    }
    
    /**
     * UDP服务端
     * 
     * @param port    端口
     * @param reuse   是否重用地址
     * @param name    服务端名称
     * @param handler 消息接收代理
     * 
     * @see Server#ADDR_LOCAL
     */
    protected UdpServer(int port, boolean reuse, String name, T handler) {
        this(ADDR_LOCAL, port, reuse, name, handler);
    }
    
    /**
     * UDP服务端
     * 
     * @param host    地址
     * @param port    端口
     * @param reuse   是否重用地址
     * @param name    服务端名称
     * @param handler 消息接收代理
     */
    protected UdpServer(String host, int port, boolean reuse, String name, T handler) {
        super(name);
        this.handler = handler;
        this.listen(host, port, reuse);
    }
    
    @Override
    protected boolean listen(String host, int port, boolean reuse) {
        LOGGER.debug("启动UDP服务端：{}-{}-{}-{}", this.name, host, port, reuse);
        boolean success = true;
        try {
            this.channel = DatagramChannel.open(NetUtils.LOCAL_PROTOCOL_FAMILY);
            // 不要阻塞
            this.channel.configureBlocking(false);
            this.channel.setOption(StandardSocketOptions.SO_REUSEADDR, reuse);
            this.channel.bind(NetUtils.buildSocketAddress(host, port));
        } catch (IOException e) {
            LOGGER.error("启动UDP服务端异常：{}", this.name, e);
            success = false;
        } finally {
            if(success) {
                LOGGER.debug("启动UDP服务端成功：{}", this.name);
            } else {
                this.close();
            }
        }
        return success;
    }
    
    /**
     * 多播（组播）
     * 
     * @param ttl   TTL
     * @param group 分组
     */
    protected void join(int ttl, String group) {
        if(!this.available()) {
            LOGGER.warn("UDP多播失败：{}-{}-{}", this.name, group, this.channel);
            return;
        }
        try {
            this.channel.setOption(StandardSocketOptions.IP_MULTICAST_TTL, ttl);
            this.channel.setOption(StandardSocketOptions.IP_MULTICAST_LOOP, true);
            this.channel.join(InetAddress.getByName(group), NetUtils.DEFAULT_NETWORK_INTERFACE);
        } catch (IOException e) {
            LOGGER.debug("UDP多播异常：{}-{}", this.name, group, e);
        }
    }
    
    /**
     * 消息代理
     */
    protected void handle() {
        if(!this.available()) {
            LOGGER.warn("UDP消息代理失败：{}-{}", this.name, this.channel);
            return;
        }
        this.handler.handle(this.channel);
        this.selector();
        EXECUTOR.submit(this::loopMessage);
    }
    
    /**
     * 注册消息读取事件
     */
    private void selector() {
        try {
            this.selector = Selector.open();
            this.channel.register(this.selector, SelectionKey.OP_READ);
        } catch (IOException e) {
            LOGGER.error("注册消息读取事件异常：{}", this.name, e);
        }
    }
    
    /**
     * 消息轮询
     */
    private void loopMessage() {
        while (this.available()) {
            try {
                this.receive();
            } catch (Exception e) {
                LOGGER.error("UDP Server消息轮询异常：{}", this.name, e);
            }
        }
        LOGGER.debug("UDP Server退出消息轮询：{}", this.name);
    }
    
    /**
     * 消息接收
     * 
     * @throws IOException IO异常
     */
    private void receive() throws IOException {
        if(this.selector.select() > 0) {
            final Set<SelectionKey> selectionKeys = this.selector.selectedKeys();
            final Iterator<SelectionKey> iterator = selectionKeys.iterator();
            while (iterator.hasNext()) {
                // UDP和TCP不同连接断开不用移除Key
                final SelectionKey selectionKey = iterator.next();
                // 移除已经取出来的信息
                iterator.remove();
                if (selectionKey.isValid() && selectionKey.isReadable()) {
                    final ByteBuffer buffer = ByteBuffer.allocateDirect(SystemConfig.UDP_BUFFER_LENGTH);
                    // 服务端多例：selectionKey.channel()
                    // 服务端单例：客户端通道=服务端通道
                    final InetSocketAddress socketAddress = (InetSocketAddress) this.channel.receive(buffer);
                    this.handler.receive(buffer, socketAddress);
                }
            }
        }
    }
    
    /**
     * 关闭UDP Server
     */
    public void close() {
        LOGGER.debug("关闭UDP Server：{}", this.name);
        IoUtils.close(this.channel);
        IoUtils.close(this.selector);
    }
    
    /**
     * 关闭UDP Server线程池
     */
    public static final void shutdown() {
        LOGGER.debug("关闭UDP Server线程池");
        SystemThreadContext.shutdown(EXECUTOR);
    }

}
