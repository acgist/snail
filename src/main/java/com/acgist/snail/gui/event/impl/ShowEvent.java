package com.acgist.snail.gui.event.impl;

import com.acgist.snail.gui.event.GuiEvent;
import com.acgist.snail.gui.main.MainWindow;

import javafx.application.Platform;

/**
 * GUI显示窗口事件
 * 
 * @author acgist
 * @since 1.1.0
 */
public class ShowEvent extends GuiEvent {

	private static final ShowEvent INSTANCE = new ShowEvent();
	
	protected ShowEvent() {
		super(Type.show, "显示窗口事件");
	}

	@Override
	protected void executeNative(Object ... args) {
		Platform.runLater(() -> {
			MainWindow.getInstance().show();
		});
	}

	@Override
	protected void executeExtend(Object ... args) {
	}

	public static final GuiEvent getInstance() {
		return INSTANCE;
	}

}
