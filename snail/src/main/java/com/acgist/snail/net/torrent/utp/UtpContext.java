package com.acgist.snail.net.torrent.utp;

import java.net.InetSocketAddress;
import java.nio.channels.DatagramChannel;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.acgist.snail.context.IContext;
import com.acgist.snail.context.SystemThreadContext;
import com.acgist.snail.logger.Logger;
import com.acgist.snail.logger.LoggerFactory;
import com.acgist.snail.net.IChannelHandler;
import com.acgist.snail.net.MessageHandlerContext;
import com.acgist.snail.net.UdpMessageHandler;

/**
 * UTP上下文
 * 管理UTP消息代理
 * 
 * @author acgist
 */
public final class UtpContext implements IContext, IChannelHandler<DatagramChannel> {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(UtpContext.class);
    
    private static final UtpContext INSTANCE = new UtpContext();
    
    public static final UtpContext getInstance() {
        return INSTANCE;
    }
    
    /**
     * UTP超时执行周期（秒）：{@value}
     */
    private static final int UTP_TIMEOUT_INTERVAL = 10;
    
    /**
     * 连接ID
     */
    private short connectionId = (short) System.currentTimeMillis();
    /**
     * UDP通道
     */
    private DatagramChannel channel;
    /**
     * 消息代理上下文
     */
    private final MessageHandlerContext context;
    /**
     * UTP消息代理列表
     * 连接Key=消息代理
     * 
     * @see #buildKey(short, InetSocketAddress)
     */
    private final Map<String, UtpMessageHandler> utpMessageHandlers;
    
    private UtpContext() {
        this.context = MessageHandlerContext.getInstance();
        this.utpMessageHandlers = new ConcurrentHashMap<>();
        SystemThreadContext.scheduledAtFixedDelay(
            UTP_TIMEOUT_INTERVAL,
            UTP_TIMEOUT_INTERVAL,
            TimeUnit.SECONDS,
            this::timeout
        );
    }
    
    @Override
    public void handle(DatagramChannel channel) {
        this.channel = channel;
    }
    
    /**
     * @return 连接ID
     */
    public short connectionId() {
        synchronized (this) {
            return this.connectionId++;
        }
    }
    
    /**
     * @param connectionId  连接ID
     * @param socketAddress 连接地址
     * 
     * @return UTP消息代理
     */
    public UdpMessageHandler get(short connectionId, InetSocketAddress socketAddress) {
        final String key = this.buildKey(connectionId, socketAddress);
        UtpMessageHandler utpMessageHandler = this.utpMessageHandlers.get(key);
        if(utpMessageHandler != null) {
            return utpMessageHandler;
        }
        utpMessageHandler = new UtpMessageHandler(connectionId, socketAddress);
        utpMessageHandler.handle(this.channel);
        // 只需要管理服务端连接
        this.context.newInstance(utpMessageHandler);
        return utpMessageHandler;
    }
    
    /**
     * 添加UTP消息代理
     * 
     * @param utpMessageHandler UTP消息代理
     */
    public void put(UtpMessageHandler utpMessageHandler) {
        synchronized (this.utpMessageHandlers) {
            this.utpMessageHandlers.put(utpMessageHandler.key(), utpMessageHandler);
        }
    }
    
    /**
     * 删除UTP消息代理
     * 
     * @param utpMessageHandler UTP消息代理
     */
    public void remove(UtpMessageHandler utpMessageHandler) {
        synchronized (this.utpMessageHandlers) {
            this.utpMessageHandlers.remove(utpMessageHandler.key());
        }
    }
    
    /**
     * 生成UTP消息代理连接Key
     * 
     * @param connectionId  连接ID
     * @param socketAddress 请求地址
     * 
     * @return 连接Key
     */
    public String buildKey(short connectionId, InetSocketAddress socketAddress) {
        return socketAddress.getHostString() + socketAddress.getPort() + connectionId;
    }
    
    /**
     * 处理超时UTP消息
     * 如果消息代理可用：重新发送超时消息
     * 如果消息代理关闭：移除消息代理
     */
    private void timeout() {
        synchronized (this.utpMessageHandlers) {
            try {
                this.utpMessageHandlers.values().stream()
                    // 超时重试
                    .filter(UtpMessageHandler::timeoutRetry)
                    // 转换List关闭：防止关闭删除消息代理产生异常
                    .collect(Collectors.toList())
                    // 已经关闭：直接移除
                    .forEach(this::remove);
            } catch (Exception e) {
                LOGGER.error("处理超时UTP消息异常", e);
            }
        }
    }

    /**
     * 关闭UTP上下文
     */
    public void close() {
        UtpRequestQueue.getInstance().shutdown();
    }

}
