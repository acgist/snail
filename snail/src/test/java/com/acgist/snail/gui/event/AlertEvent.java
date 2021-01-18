package com.acgist.snail.gui.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.context.GuiContext;
import com.acgist.snail.gui.event.adapter.AlertEventAdapter;

/**
 * <p>GUI提示窗口事件</p>
 * 
 * @author acgist
 */
public final class AlertEvent extends AlertEventAdapter {

	private static final Logger LOGGER = LoggerFactory.getLogger(AlertEvent.class);
	
	private static final AlertEvent INSTANCE = new AlertEvent();
	
	public static final GuiEvent getInstance() {
		return INSTANCE;
	}
	
	private AlertEvent() {
	}
	
	@Override
	protected void executeExtendExtend(GuiContext.MessageType type, String title, String message) {
		super.executeExtendExtend(type, title, message);
		LOGGER.debug("收到提示窗口事件：{}-{}-{}", type, title, message);
	}

}
