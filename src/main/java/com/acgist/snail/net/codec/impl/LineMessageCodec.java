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
	 * 换行符
	 */
	private final String split;
	/**
	 * 上次没有处理完成的消息，下次收到消息拼接在一起处理。
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
		messages = this.message + messages; // 拼接上次没有处理完成的消息
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
		return this.messageCodec.encode(message) + this.split;
	}
	
}
