package com.acgist.snail.gui.extend.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.gui.event.GuiEvent;
import com.acgist.snail.gui.event.adapter.ResponseEventAdapter;

/**
 * <p>GUI响应消息事件</p>
 * 
 * @author acgist
 */
public final class ResponseEvent extends ResponseEventAdapter {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ResponseEvent.class);
	
	private static final ResponseEvent INSTANCE = new ResponseEvent();
	
	public static final GuiEvent getInstance() {
		return INSTANCE;
	}
	
	private ResponseEvent() {
	}
	
	@Override
	protected void executeExtendExtend(String message) {
		LOGGER.debug("收到响应消息：{}", message);
	}
	
}
