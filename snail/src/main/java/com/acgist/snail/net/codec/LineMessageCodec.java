package com.acgist.snail.net.codec;

import java.net.InetSocketAddress;

import com.acgist.snail.net.NetException;

/**
 * <p>行消息处理器</p>
 * 
 * @author acgist
 */
public final class LineMessageCodec extends MessageCodec<String, String> {

	/**
	 * <p>消息分隔符</p>
	 */
	private final String separator;
	/**
	 * <p>消息分隔符长度</p>
	 * 
	 * @see #separator
	 */
	private final int separatorLength;
	/**
	 * <p>消息拼接器</p>
	 * <p>由于传输协议（TCP/UDP）在传输过程中可能出现粘包拆包导致消息不完整，所以记录上次没有处理的消息合并到下次接收的消息一起处理。</p>
	 */
	private final StringBuilder messageBuilder;
	
	/**
	 * @param messageDecoder 消息处理器
	 * @param separator 消息分隔符
	 */
	public LineMessageCodec(IMessageDecoder<String> messageDecoder, String separator) {
		super(messageDecoder);
		this.separator = separator;
		this.separatorLength = separator.length();
		this.messageBuilder = new StringBuilder();
	}
	
	@Override
	public String encode(String message) {
		return message + this.separator;
	}

	@Override
	protected void doDecode(String message, InetSocketAddress address) throws NetException {
		String messageLine;
		this.messageBuilder.append(message);
		int index = this.messageBuilder.indexOf(this.separator);
		while(index >= 0) {
			messageLine = this.messageBuilder.substring(0, index);
			this.doNext(messageLine, address);
			this.messageBuilder.delete(0, index + this.separatorLength);
			index = this.messageBuilder.indexOf(this.separator);
		}
	}

}
