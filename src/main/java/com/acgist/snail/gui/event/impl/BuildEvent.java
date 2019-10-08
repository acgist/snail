package com.acgist.snail.gui.event.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.gui.GuiHandler;
import com.acgist.snail.gui.event.GuiEvent;
import com.acgist.snail.gui.main.MainWindow;
import com.acgist.snail.gui.menu.TrayMenu;
import com.acgist.snail.system.context.SystemThreadContext;

import javafx.application.Platform;

/**
 * GUI创建窗口事件
 * 
 * @author acgist
 * @since 1.1.0
 */
public class BuildEvent extends GuiEvent {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(BuildEvent.class);

	private static final BuildEvent INSTANCE = new BuildEvent();
	
	protected BuildEvent() {
		super(Type.build, "创建窗口事件");
	}

	public static final GuiEvent getInstance() {
		return INSTANCE;
	}
	
	@Override
	protected void executeNative(Object ... args) {
		LOGGER.info("创建窗口");
		final Thread javaFXThread = new Thread();
		javaFXThread.setName(SystemThreadContext.SNAIL_THREAD_PLATFORM);
		javaFXThread.setDaemon(true);
		Platform.startup(javaFXThread);
		Platform.runLater(() -> {
			TrayMenu.getInstance();
			MainWindow.getInstance().show();
		});
	}

	@Override
	protected void executeExtend(Object ... args) {
		GuiHandler.getInstance().lock(); // 扩展GUI锁定程序
	}
	
}
