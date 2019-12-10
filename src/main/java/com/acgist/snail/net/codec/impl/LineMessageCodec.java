package com.acgist.snail.net.codec.impl;

import java.net.InetSocketAddress;

import com.acgist.snail.net.codec.IMessageCodec;
import com.acgist.snail.net.codec.MessageCodec;
import com.acgist.snail.system.exception.NetException;

/**
 * <p>行消息处理器</p>
 * 
 * @author acgist
 * @since 1.1.1
 */
public final class LineMessageCodec extends MessageCodec<String, String> {

	/**
	 * <p>消息分隔符</p>
	 * <p>使用消息分隔符区分多条消息</p>
	 */
	private final String split;
	/**
	 * <p>上次没有处理完成的消息</p>
	 * <p>由于传输协议（TCP/UDP）在传输过程中可能会出现粘包拆包导致消息不完整，所以记录上次没有处理完成的不完整消息，合并到下次接收的消息一起处理。</p>
	 */
	private String message = "";
	
	public LineMessageCodec(IMessageCodec<String> messageCodec, String split) {
		super(messageCodec);
		this.split = split;
	}

	@Override
	protected void decode(String messages, InetSocketAddress address, boolean haveAddress) throws NetException {
		String message;
		final int length = this.split.length();
		messages = this.message + messages; // 合并上次没有处理完成的消息
		if(messages.contains(this.split)) {
			int index = messages.indexOf(this.split);
			while(index >= 0) {
				message = messages.substring(0, index);
				this.doNext(message, address, haveAddress);
				messages = messages.substring(index + length);
				index = messages.indexOf(this.split);
			}
		}
		this.message = messages;
	}
	
	@Override
	public String encode(String message) {
		// 拼接消息分隔符
		return this.messageCodec.encode(message) + this.split;
	}
	
}
