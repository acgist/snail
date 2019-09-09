package com.acgist.snail.gui.event.impl;

import com.acgist.snail.gui.event.GuiEvent;
import com.acgist.snail.gui.main.MainWindow;

import javafx.application.Platform;

/**
 * GUI隐藏窗口事件
 * 
 * @author acgist
 * @since 1.1.0
 */
public class HideEvent extends GuiEvent {

	private static final HideEvent INSTANCE = new HideEvent();
	
	protected HideEvent() {
		super(Type.hide, "隐藏窗口事件");
	}

	@Override
	protected void executeNative(Object ... args) {
		Platform.runLater(() -> {
			MainWindow.getInstance().hide();
		});
	}

	@Override
	protected void executeExtend(Object ... args) {
	}

	public static final GuiEvent getInstance() {
		return INSTANCE;
	}
	
}
