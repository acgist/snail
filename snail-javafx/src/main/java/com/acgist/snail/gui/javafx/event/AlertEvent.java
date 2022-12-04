package com.acgist.snail.gui.javafx.event;

import com.acgist.snail.gui.GuiContext;
import com.acgist.snail.gui.event.GuiEvent;
import com.acgist.snail.gui.event.adapter.AlertEventAdapter;
import com.acgist.snail.gui.javafx.Alerts;

import javafx.application.Platform;

/**
 * <p>GUI窗口消息事件</p>
 * 
 * @author acgist
 */
public final class AlertEvent extends AlertEventAdapter {

	private static final AlertEvent INSTANCE = new AlertEvent();
	
	public static final GuiEvent getInstance() {
		return INSTANCE;
	}

	private AlertEvent() {
	}
	
	@Override
	protected void executeNativeExtend(GuiContext.MessageType type, String title, String message) {
		Platform.runLater(() -> Alerts.build(title, message, type));
	}

}
