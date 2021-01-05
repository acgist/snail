package com.acgist.snail.net.codec;

import java.net.InetSocketAddress;

import com.acgist.snail.context.exception.NetException;
import com.acgist.snail.utils.StringUtils;

/**
 * <p>多行消息处理器</p>
 * 
 * @author acgist
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
	private final String multilineRegex;
	/**
	 * <p>多行消息</p>
	 */
	private final StringBuilder multilineMessage;
	
	/**
	 * @param messageCodec 下一个消息处理器
	 * @param separator 消息分隔符
	 * @param multilineRegex 多行消息结束符
	 */
	public MultilineMessageCodec(IMessageCodec<String> messageCodec, String separator, String multilineRegex) {
		super(messageCodec);
		this.separator = separator;
		this.multilineRegex = multilineRegex;
		this.multilineMessage = new StringBuilder();
	}

	@Override
	protected void doDecode(String message, InetSocketAddress address) throws NetException {
		if(StringUtils.regex(message, this.multilineRegex, false)) {
			this.multilineMessage.append(message);
			this.doNext(this.multilineMessage.toString(), address);
			this.multilineMessage.setLength(0);
		} else {
			this.multilineMessage.append(message).append(this.separator);
		}
	}

}
