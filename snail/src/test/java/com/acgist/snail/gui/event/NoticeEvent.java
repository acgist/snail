package com.acgist.snail.gui.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.gui.GuiManager;
import com.acgist.snail.gui.event.adapter.NoticeEventAdapter;

/**
 * <p>GUI提示消息事件</p>
 * 
 * @author acgist
 */
public final class NoticeEvent extends NoticeEventAdapter {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(NoticeEvent.class);
	
	private static final NoticeEvent INSTANCE = new NoticeEvent();
	
	public static final GuiEvent getInstance() {
		return INSTANCE;
	}
	
	private NoticeEvent() {
	}
	
	@Override
	protected void executeExtendExtend(GuiManager.MessageType type, String title, String message) {
		super.executeExtendExtend(type, title, message);
		LOGGER.debug("收到提示消息事件：{}-{}-{}", type, title, message);
	}
	
}
