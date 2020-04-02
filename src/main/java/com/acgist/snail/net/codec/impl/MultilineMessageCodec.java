package com.acgist.snail.net.codec.impl;

import java.net.InetSocketAddress;

import com.acgist.snail.net.codec.IMessageCodec;
import com.acgist.snail.net.codec.MessageCodec;
import com.acgist.snail.system.exception.NetException;
import com.acgist.snail.utils.StringUtils;

/**
 * <p>多行消息处理器</p>
 * <p>必须配合{@linkplain LineMessageCodec 行消息处理器}一起使用</p>
 * 
 * @author acgist
 * @since 1.1.1
 */
public final class MultilineMessageCodec extends MessageCodec<String, String> {

	/**
	 * <p>消息分隔符</p>
	 * <p>使用消息分隔符区分多条消息</p>
	 */
	private final String separator;
	/**
	 * <p>多行消息结束符（正则表达式）</p>
	 * <p>消息匹配多行消息结束符成功时合并为一条消息</p>
	 */
	private final String endRegex;
	/**
	 * <p>消息</p>
	 */
	private final StringBuilder message;
	
	public MultilineMessageCodec(IMessageCodec<String> messageCodec, String separator, String endRegex) {
		super(messageCodec);
		this.separator = separator;
		this.endRegex = endRegex;
		this.message = new StringBuilder();
	}

	@Override
	protected void decode(String message, InetSocketAddress address, boolean haveAddress) throws NetException {
		if(StringUtils.regex(message, this.endRegex, false)) {
			this.message.append(message);
			this.doNext(this.message.toString(), address, haveAddress);
			this.message.setLength(0);
		} else {
			this.message.append(message).append(this.separator);
		}
	}

}
