package com.acgist.snail.gui.event;

import com.acgist.snail.gui.event.adapter.ExitEventAdapter;

/**
 * <p>GUI退出窗口事件</p>
 * 
 * @author acgist
 */
public final class ExitEvent extends ExitEventAdapter {

	private static final ExitEvent INSTANCE = new ExitEvent();
	
	public static final GuiEvent getInstance() {
		return INSTANCE;
	}
	
	private ExitEvent() {
	}
	
}
