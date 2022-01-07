package com.acgist.snail.gui.javafx.event;

import com.acgist.snail.gui.event.GuiEvent;
import com.acgist.snail.gui.event.adapter.BuildEventAdapter;
import com.acgist.snail.gui.javafx.menu.TrayMenu;
import com.acgist.snail.gui.javafx.window.main.MainWindow;
import com.acgist.snail.logger.Logger;
import com.acgist.snail.logger.LoggerFactory;

import javafx.application.Platform;

/**
 * <p>GUI新建窗口事件</p>
 * 
 * @author acgist
 */
public final class BuildEvent extends BuildEventAdapter {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(BuildEvent.class);

	private static final BuildEvent INSTANCE = new BuildEvent();
	
	public static final GuiEvent getInstance() {
		return INSTANCE;
	}
	
	private BuildEvent() {
	}
	
	@Override
	protected void executeNative(Object ... args) {
		LOGGER.debug("新建GUI窗口");
		Platform.startup(() -> {});
		Platform.runLater(() -> {
			TrayMenu.getInstance();
			MainWindow.getInstance().show();
		});
	}

}
