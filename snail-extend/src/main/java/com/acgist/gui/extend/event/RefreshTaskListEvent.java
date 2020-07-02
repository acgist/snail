package com.acgist.gui.extend.event;

import com.acgist.snail.gui.event.GuiEvent;
import com.acgist.snail.gui.event.adapter.RefreshTaskListEventAdapter;

/**
 * <p>GUI刷新任务列表事件</p>
 * 
 * @author acgist
 * @since 1.1.0
 */
public final class RefreshTaskListEvent extends RefreshTaskListEventAdapter {

	private static final RefreshTaskListEvent INSTANCE = new RefreshTaskListEvent();
	
	public static final GuiEvent getInstance() {
		return INSTANCE;
	}

}
