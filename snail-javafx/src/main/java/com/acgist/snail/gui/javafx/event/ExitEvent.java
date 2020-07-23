package com.acgist.snail.gui.javafx.event;

import com.acgist.snail.gui.event.GuiEvent;
import com.acgist.snail.gui.event.adapter.ExitEventAdapter;
import com.acgist.snail.gui.javafx.menu.TrayMenu;

import javafx.application.Platform;

/**
 * <p>GUI退出窗口事件</p>
 * 
 * @author acgist
 * @since 1.1.0
 */
public final class ExitEvent extends ExitEventAdapter {

	private static final ExitEvent INSTANCE = new ExitEvent();
	
	public static final GuiEvent getInstance() {
		return INSTANCE;
	}
	
	private ExitEvent() {
	}

	@Override
	protected void executeNative(Object ... args) {
		Platform.exit(); // 退出平台
		TrayMenu.exit(); // 退出托盘
	}

}
