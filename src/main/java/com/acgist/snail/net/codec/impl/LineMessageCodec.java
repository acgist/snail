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
	private final String separator;
	/**
	 * <p>上次没有处理完成的消息</p>
	 * <p>由于传输协议（TCP/UDP）在传输过程中可能会出现粘包拆包导致消息不完整，所以记录上次没有处理完成的不完整消息，合并到下次接收的消息一起处理。</p>
	 */
	private String lastMessage = "";
	
	/**
	 * <p>行消息处理器</p>
	 * 
	 * @param messageCodec 下一个消息处理器
	 * @param separator 消息分隔符
	 */
	public LineMessageCodec(IMessageCodec<String> messageCodec, String separator) {
		super(messageCodec);
		this.separator = separator;
	}

	@Override
	protected void decode(String message, InetSocketAddress address, boolean haveAddress) throws NetException {
		String messageLine; // 独立消息
		final int length = this.separator.length();
		message = this.lastMessage + message; // 合并上次没有处理完成的消息
		if(message.contains(this.separator)) {
			int index = message.indexOf(this.separator);
			while(index >= 0) {
				messageLine = message.substring(0, index);
				this.doNext(messageLine, address, haveAddress);
				message = message.substring(index + length);
				index = message.indexOf(this.separator);
			}
		}
		this.lastMessage = message;
	}
	
	@Override
	public String encode(String message) {
		// 拼接消息分隔符
		return this.messageCodec.encode(message) + this.separator;
	}
	
}
