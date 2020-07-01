package com.acgist.snail.gui.javafx.event;

import com.acgist.snail.gui.GuiManager.SnailAlertType;
import com.acgist.snail.gui.event.GuiEvent;
import com.acgist.snail.gui.event.adapter.AlertEventAdapter;
import com.acgist.snail.gui.javafx.Alerts;

/**
 * <p>GUI提示窗口事件</p>
 * 
 * @author acgist
 * @since 1.1.0
 */
public final class AlertEvent extends AlertEventAdapter {

	private static final AlertEvent INSTANCE = new AlertEvent();
	
	public static final GuiEvent getInstance() {
		return INSTANCE;
	}

	/**
	 * <p>本地提示窗口</p>
	 * 
	 * @param type 类型
	 * @param title 标题
	 * @param message 消息
	 */
	@Override
	protected void executeNativeExtend(SnailAlertType type, String title, String message) {
		Alerts.build(title, message, type);
	}

}
