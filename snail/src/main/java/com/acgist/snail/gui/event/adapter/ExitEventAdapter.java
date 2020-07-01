package com.acgist.snail.gui.event.adapter;

import com.acgist.snail.gui.GuiManager;
import com.acgist.snail.gui.event.GuiEvent;

/**
 * <p>GUI退出窗口事件</p>
 * 
 * @author acgist
 * @since 1.1.0
 */
public abstract class ExitEventAdapter extends GuiEvent {

	protected ExitEventAdapter() {
		super(Type.EXIT, "退出窗口事件");
	}

	@Override
	protected final void executeExtend(Object ... args) {
		GuiManager.getInstance().unlock(); // 释放扩展GUI阻塞锁
	}
	
}
