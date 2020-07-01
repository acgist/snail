package com.acgist.snail.gui.javafx.event;

import com.acgist.snail.gui.event.GuiEvent;
import com.acgist.snail.gui.event.adapter.ShowEventAdapter;
import com.acgist.snail.gui.javafx.main.MainWindow;

import javafx.application.Platform;

/**
 * <p>GUI显示窗口事件</p>
 * 
 * @author acgist
 * @since 1.1.0
 */
public final class ShowEvent extends ShowEventAdapter {

	private static final ShowEvent INSTANCE = new ShowEvent();
	
	public static final GuiEvent getInstance() {
		return INSTANCE;
	}
	
	@Override
	protected void executeNative(Object ... args) {
		Platform.runLater(() -> MainWindow.getInstance().show());
	}

}
