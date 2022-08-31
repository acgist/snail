package com.acgist.snail.net.codec;

import java.net.InetSocketAddress;

import com.acgist.snail.context.exception.NetException;

/**
 * <p>消息处理器</p>
 * <table border="1">
 * 	<caption>消息处理器</caption>
 * 	<tr>
 * 		<th>功能</th><th>实现</th>
 * 	</tr>
 * 	<tr>
 * 		<td>消息解码</td>
 * 		<td>继承{@link MessageCodec}</td>
 * 	</tr>
 * 	<tr>
 * 		<td>消息处理</td>
 * 		<td>实现{@link IMessageDecoder}</td>
 * 	</tr>
 * 	<tr>
 * 		<td>消息编码</td>
 * 		<td>实现{@link IMessageEncoder}</td>
 * 	</tr>
 * </table>
 * 
 * 消息解码一般都是从头到尾全部执行，但是消息编码却不一定，所以编码需要自己指定编码器和编码顺序。
 * 
 * @param <I> 输入消息类型
 * @param <O> 输出消息类型
 * 
 * @author acgist
 */
public abstract class MessageCodec<I, O> implements IMessageDecoder<I>, IMessageEncoder<I> {

	/**
	 * <p>消息处理器</p>
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
	 * <p>消息解码</p>
	 * <p>解码完成必须执行{@link #doNext(Object, InetSocketAddress)}方法</p>
	 * 
	 * @param message 消息
	 * @param address 地址
	 * 
	 * @throws NetException 网络异常
	 */
	protected abstract void doDecode(I message, InetSocketAddress address) throws NetException;
	
	/**
	 * <p>执行消息处理器</p>
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
	 * <p>消息处理请实现{@link IMessageDecoder}</p>
	 */
	@Override
	public final void onMessage(I message) throws NetException {
		throw new NetException("消息处理器不能直接处理消息");
	}

	/**
	 * {@inheritDoc}
	 * 
	 * <p>消息处理请实现{@link IMessageDecoder}</p>
	 */
	@Override
	public final void onMessage(I message, InetSocketAddress address) throws NetException {
		throw new NetException("消息处理器不能直接处理消息");
	}
	
}
