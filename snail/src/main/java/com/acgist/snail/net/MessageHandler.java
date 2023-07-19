package com.acgist.snail.net;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.Channel;

import com.acgist.snail.net.codec.IMessageDecoder;

/**
 * 消息代理
 *
 * @param <T> 通道代理类型
 * 
 * @author acgist
 */
public abstract class MessageHandler<T extends Channel> implements IMessageHandler, IChannelHandler<T> {

    /**
     * 是否关闭
     */
    protected volatile boolean close = false;
    /**
     * 通道
     */
    protected T channel;
    /**
     * 消息处理器
     */
    protected IMessageDecoder<ByteBuffer> messageDecoder;
    
    @Override
    public final boolean available() {
        return
            !this.close          &&
            this.channel != null &&
            this.channel.isOpen();
    }
    
    @Override
    public void onReceive(ByteBuffer buffer) throws NetException {
        if(this.messageDecoder == null) {
            throw new NetException("请设置消息处理器或重新接收消息方法");
        }
        this.messageDecoder.decode(buffer);
    }
    
    @Override
    public void onReceive(ByteBuffer buffer, InetSocketAddress socketAddress) throws NetException {
        if(this.messageDecoder == null) {
            throw new NetException("请设置消息处理器或重新接收消息方法");
        }
        this.messageDecoder.decode(buffer, socketAddress);
    }
    
}
