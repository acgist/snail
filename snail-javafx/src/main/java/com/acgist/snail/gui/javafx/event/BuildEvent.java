package com.acgist.snail.gui.javafx.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.context.SystemThreadContext;
import com.acgist.snail.gui.event.GuiEvent;
import com.acgist.snail.gui.event.adapter.BuildEventAdapter;
import com.acgist.snail.gui.javafx.main.MainWindow;
import com.acgist.snail.gui.javafx.menu.TrayMenu;

import javafx.application.Platform;

/**
 * <p>GUI创建窗口事件</p>
 * 
 * @author acgist
 * @since 1.1.0
 */
public final class BuildEvent extends BuildEventAdapter {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(BuildEventAdapter.class);

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
		Platform.startup(javaFXThread); // 异步启动
		Platform.runLater(() -> {
			TrayMenu.getInstance();
			MainWindow.getInstance().show();
		});
	}

}
