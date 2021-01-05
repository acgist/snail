package com.acgist.snail.net.codec;

import java.net.InetSocketAddress;

import com.acgist.snail.context.exception.NetException;

/**
 * <p>行消息处理器</p>
 * 
 * @author acgist
 */
public final class LineMessageCodec extends MessageCodec<String, String> {

	/**
	 * <p>消息分隔符</p>
	 * <p>使用消息分隔符区分多条消息</p>
	 */
	private final String separator;
	/**
	 * <p>消息拼接器</p>
	 * <p>由于传输协议（TCP/UDP）在传输过程中可能会出现粘包拆包导致消息不完整，所以记录上次没有处理完成的不完整消息，合并到下次接收的消息一起处理。</p>
	 */
	private final StringBuilder messageBuilder;
	
	/**
	 * @param messageCodec 下一个消息处理器
	 * @param separator 消息分隔符
	 */
	public LineMessageCodec(IMessageCodec<String> messageCodec, String separator) {
		super(messageCodec);
		this.separator = separator;
		this.messageBuilder = new StringBuilder();
	}
	
	@Override
	public String encode(String message) {
		// 拼接消息分隔符
		return super.encode(message) + this.separator;
	}

	@Override
	protected void doDecode(String message, InetSocketAddress address) throws NetException {
		String messageLine; // 独立消息
		this.messageBuilder.append(message); // 合并上次没有处理完成的消息
		final int length = this.separator.length();
		int index = this.messageBuilder.indexOf(this.separator);
		while(index >= 0) {
			messageLine = this.messageBuilder.substring(0, index);
			this.doNext(messageLine, address);
			this.messageBuilder.delete(0, index + length);
			index = this.messageBuilder.indexOf(this.separator);
		}
	}

}
