package com.acgist.snail.gui.extend.event;

import com.acgist.snail.gui.event.GuiEvent;
import com.acgist.snail.gui.event.adapter.RefreshTaskStatusEventAdapter;

/**
 * <p>GUI刷新任务状态事件</p>
 * 
 * @author acgist
 * @since 1.4.0
 */
public final class RefreshTaskStatusEvent extends RefreshTaskStatusEventAdapter {

	private static final RefreshTaskStatusEvent INSTANCE = new RefreshTaskStatusEvent();
	
	public static final GuiEvent getInstance() {
		return INSTANCE;
	}
	
	private RefreshTaskStatusEvent() {
	}
	
}
