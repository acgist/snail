package com.acgist.snail.gui.extend.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.gui.GuiManager;
import com.acgist.snail.gui.event.GuiEvent;
import com.acgist.snail.gui.event.adapter.AlertEventAdapter;

/**
 * <p>GUI提示窗口事件</p>
 * 
 * @author acgist
 * @since 1.4.0
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
	protected void executeExtendExtend(GuiManager.MessageType type, String title, String message) {
		LOGGER.debug("收到提示窗口信息：{}-{}-{}", type, title, message);
	}

}
