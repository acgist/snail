package com.acgist.snail.net.codec;

import java.net.InetSocketAddress;

import com.acgist.snail.net.NetException;

/**
 * 消息处理器
 * 
 * 功能      实现
 * 消息解码   继承{@link MessageCodec}
 * 消息处理   实现{@link IMessageDecoder}
 * 消息编码   实现{@link IMessageEncoder}
 * 
 * @param <I> 输入消息类型
 * @param <O> 输出消息类型
 * 
 * @author acgist
 */
public abstract class MessageCodec<I, O> implements IMessageDecoder<I>, IMessageEncoder<I> {

    /**
     * 消息处理器
     */
    protected final IMessageDecoder<O> messageDecoder;

    /**
     * @param messageDecoder 消息处理器
     */
    protected MessageCodec(IMessageDecoder<O> messageDecoder) {
        this.messageDecoder = messageDecoder;
    }

    @Override
    public final boolean done() {
        return false;
    }
    
    @Override
    public final void decode(I message) throws NetException {
        this.doDecode(message, null);
    }

    @Override
    public final void decode(I message, InetSocketAddress address) throws NetException {
        this.doDecode(message, address);
    }
    
    /**
     * 消息解码
     * 解码完成必须执行{@link #doNext(Object, InetSocketAddress)}方法
     * 
     * @param message 消息
     * @param address 地址
     * 
     * @throws NetException 网络异常
     */
    protected abstract void doDecode(I message, InetSocketAddress address) throws NetException;
    
    /**
     * 执行消息处理器
     * 
     * @param message 消息
     * @param address 地址
     * 
     * @throws NetException 网络异常
     */
    protected void doNext(O message, InetSocketAddress address) throws NetException {
        if(address != null) {
            if(this.messageDecoder.done()) {
                this.messageDecoder.onMessage(message, address);
            } else {
                this.messageDecoder.decode(message, address);
            }
        } else {
            if(this.messageDecoder.done()) {
                this.messageDecoder.onMessage(message);
            } else {
                this.messageDecoder.decode(message);
            }
        }
    }
    
    /**
     * {@inheritDoc}
     * 
     * 消息处理请实现{@link IMessageDecoder}
     */
    @Override
    public final void onMessage(I message) throws NetException {
        throw new NetException("消息处理器不能直接处理消息");
    }

    /**
     * {@inheritDoc}
     * 
     * 消息处理请实现{@link IMessageDecoder}
     */
    @Override
    public final void onMessage(I message, InetSocketAddress address) throws NetException {
        throw new NetException("消息处理器不能直接处理消息");
    }
    
}
