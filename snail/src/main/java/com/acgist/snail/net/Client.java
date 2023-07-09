package com.acgist.snail.net;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

/**
 * 客户端
 * 
 * @param <T> 消息代理类型
 * 
 * @author acgist
 */
public abstract class Client<T extends IMessageSender> implements IMessageSender {

    /**
     * 客户端名称
     */
    protected final String name;
    /**
     * 消息代理
     */
    protected final T handler;
    
    /**
     * @param name    客户端名称
     * @param handler 消息代理
     */
    protected Client(String name, T handler) {
        this.name    = name;
        this.handler = handler;
    }

    @Override
    public boolean available() {
        if(this.handler == null) {
            return false;
        } else {
            return this.handler.available();
        }
    }

    @Override
    public void send(String message) throws NetException {
        this.handler.send(message);
    }

    @Override
    public void send(String message, String charset) throws NetException {
        this.handler.send(message, charset);
    }
    
    @Override
    public void send(byte[] message) throws NetException {
        this.handler.send(message);
    }

    @Override
    public void send(ByteBuffer buffer) throws NetException {
        this.handler.send(buffer);
    }
    
    @Override
    public void send(ByteBuffer buffer, int timeout) throws NetException {
        this.handler.send(buffer, timeout);
    }
    
    @Override
    public InetSocketAddress remoteSocketAddress() {
        return this.handler.remoteSocketAddress();
    }

    @Override
    public void close() {
        if(this.handler == null) {
            return;
        }
        this.handler.close();
    }
    
}
