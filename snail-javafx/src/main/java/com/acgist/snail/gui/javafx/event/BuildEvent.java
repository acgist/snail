package com.acgist.snail.gui.javafx.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.context.SystemThreadContext;
import com.acgist.snail.gui.event.GuiEvent;
import com.acgist.snail.gui.event.adapter.BuildEventAdapter;
import com.acgist.snail.gui.javafx.menu.TrayMenu;
import com.acgist.snail.gui.javafx.window.main.MainWindow;

import javafx.application.Platform;

/**
 * <p>GUI创建窗口事件</p>
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
		LOGGER.info("创建GUI窗口");
		final Thread javaFXThread = new Thread();
		javaFXThread.setName(SystemThreadContext.SNAIL_THREAD_PLATFORM);
		javaFXThread.setDaemon(true);
		// 阻塞线程
		Platform.startup(javaFXThread);
		Platform.runLater(() -> {
			TrayMenu.getInstance();
			MainWindow.getInstance().show();
		});
	}

}
