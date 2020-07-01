package com.acgist.snail.gui.event.impl;

import com.acgist.snail.gui.GuiManager;
import com.acgist.snail.gui.event.GuiEvent;
import com.acgist.snail.gui.menu.TrayMenu;

import javafx.application.Platform;

/**
 * <p>GUI退出窗口事件</p>
 * 
 * @author acgist
 * @since 1.1.0
 */
public final class ExitEvent extends GuiEvent {

	private static final ExitEvent INSTANCE = new ExitEvent();
	
	protected ExitEvent() {
		super(Type.EXIT, "退出窗口事件");
	}
	
	public static final GuiEvent getInstance() {
		return INSTANCE;
	}

	@Override
	protected void executeNative(Object ... args) {
		Platform.exit(); // 退出平台
		TrayMenu.exit(); // 退出托盘
	}

	@Override
	protected void executeExtend(Object ... args) {
		GuiManager.getInstance().unlock(); // 释放扩展GUI阻塞锁
	}
	
}
