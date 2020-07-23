package com.acgist.snail.gui.javafx.event;

import com.acgist.snail.gui.event.GuiEvent;
import com.acgist.snail.gui.event.adapter.HideEventAdapter;
import com.acgist.snail.gui.javafx.main.MainWindow;

import javafx.application.Platform;

/**
 * <p>GUI隐藏窗口事件</p>
 * 
 * @author acgist
 * @since 1.1.0
 */
public final class HideEvent extends HideEventAdapter {

	private static final HideEvent INSTANCE = new HideEvent();
	
	public static final GuiEvent getInstance() {
		return INSTANCE;
	}
	
	private HideEvent() {
	}

	@Override
	protected void executeNative(Object ... args) {
		Platform.runLater(() -> MainWindow.getInstance().hide());
	}

}
