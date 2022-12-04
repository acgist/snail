package com.acgist.snail.gui;

import com.acgist.snail.format.BEncodeDecoder;
import com.acgist.snail.gui.event.GuiEventMessage;
import com.acgist.snail.net.PacketSizeException;
import com.acgist.snail.net.application.ApplicationMessage;
import com.acgist.snail.net.application.ApplicationMessage.Type;

/**
 * <p>GUI消息</p>
 * 
 * @author acgist
 * 
 * @see Type#ALERT
 * @see Type#NOTICE
 */
public final record GuiMessage(
	/**
	 * <p>消息类型</p>
	 */
	GuiContext.MessageType type,
	/**
	 * <p>消息标题</p>
	 */
	String title,
	/**
	 * <p>消息内容</p>
	 */
	String message
) {
	
	/**
	 * <p>通过系统消息读取GUI消息</p>
	 * 
	 * @param message 系统消息
	 * 
	 * @return GUI消息
	 * 
	 * @throws PacketSizeException 网络包大小异常
	 */
	public static final GuiMessage of(ApplicationMessage message) throws PacketSizeException {
		final String body = message.getBody();
		final var decoder = BEncodeDecoder.newInstance(body).next();
		if(decoder.isEmpty()) {
			return null;
		}
		final String type = decoder.getString(GuiEventMessage.MESSAGE_TYPE);
		final String title = decoder.getString(GuiEventMessage.MESSAGE_TITLE);
		final String content = decoder.getString(GuiEventMessage.MESSAGE_MESSAGE);
		return new GuiMessage(GuiContext.MessageType.of(type), title, content);
	}
	
}
