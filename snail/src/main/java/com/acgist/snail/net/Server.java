package com.acgist.snail.net;

import java.nio.channels.Channel;

/**
 * 服务端
 * 注意：连接接入以后注意管理（弱引用或者管理器）防止内存泄露
 * 
 * @author acgist
 */
public abstract class Server<T extends Channel> {

    /**
     * TTL：{@value}
     */
    public static final int TTL = 2;
    /**
     * 随机端口：{@value}
     */
    public static final int PORT_AUTO = 0;
    /**
     * 本机地址
     */
    public static final String ADDR_LOCAL = null;
    /**
     * 重用地址：{@value}
     */
    public static final boolean ADDR_REUSE = true;
    /**
     * 禁止重用地址：{@value}
     */
    public static final boolean ADDR_UNREUSE = false;
    
    /**
     * 服务端名称
     */
    protected final String name;
    
    /**
     * 服务器通道
     */
    protected T channel;

    /**
     * @param name 服务端名称
     */
    protected Server(String name) {
        this.name = name;
    }
    
    /**
     * 打开监听
     * 
     * @param host  地址
     * @param port  端口
     * @param reuse 是否重用
     * 
     * @return 是否监听成功
     */
    abstract boolean listen(String host, int port, boolean reuse);
    
    /**
     * @return 是否可用
     */
    public final boolean available() {
        return this.channel != null && this.channel.isOpen();
    }
    
    /**
     * @return 服务端通道
     */
    public final T getChannel() {
        return this.channel;
    }
    
}
