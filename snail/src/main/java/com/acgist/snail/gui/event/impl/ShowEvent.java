package com.acgist.snail.gui.event.impl;

import com.acgist.snail.gui.event.GuiEvent;
import com.acgist.snail.gui.main.MainWindow;

import javafx.application.Platform;

/**
 * <p>GUI显示窗口事件</p>
 * 
 * @author acgist
 * @since 1.1.0
 */
public final class ShowEvent extends GuiEvent {

	private static final ShowEvent INSTANCE = new ShowEvent();
	
	protected ShowEvent() {
		super(Type.SHOW, "显示窗口事件");
	}

	public static final GuiEvent getInstance() {
		return INSTANCE;
	}
	
	@Override
	protected void executeNative(Object ... args) {
		Platform.runLater(() -> MainWindow.getInstance().show());
	}

	@Override
	protected void executeExtend(Object ... args) {
		// TODO：实现
	}

}
