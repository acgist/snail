package com.acgist.snail.net;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.Channel;

import com.acgist.snail.context.exception.NetException;
import com.acgist.snail.net.codec.IMessageDecoder;

/**
 * <p>消息代理</p>
 *
 * @param <T> 通道代理类型
 * 
 * @author acgist
 */
public abstract class MessageHandler<T extends Channel> implements IMessageHandler, IChannelHandler<T> {

	/**
	 * <p>是否关闭</p>
	 */
	protected volatile boolean close = false;
	/**
	 * <p>通道</p>
	 */
	protected T channel;
	/**
	 * <p>消息处理器</p>
	 */
	protected IMessageDecoder<ByteBuffer> messageDecoder;
	
	@Override
	public final boolean available() {
		return !this.close && this.channel != null && this.channel.isOpen();
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
