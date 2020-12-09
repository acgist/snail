package com.acgist.snail.gui.extend.event;

import com.acgist.snail.gui.event.GuiEvent;
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
