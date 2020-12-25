package com.acgist.snail.gui.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.gui.event.adapter.RefreshTaskListEventAdapter;

/**
 * <p>GUI刷新任务列表事件</p>
 * 
 * @author acgist
 */
public final class RefreshTaskListEvent extends RefreshTaskListEventAdapter {

	private static final Logger LOGGER = LoggerFactory.getLogger(RefreshTaskListEvent.class);
	
	private static final RefreshTaskListEvent INSTANCE = new RefreshTaskListEvent();
	
	public static final GuiEvent getInstance() {
		return INSTANCE;
	}
	
	private RefreshTaskListEvent() {
	}

	@Override
	protected void executeExtend(Object... args) {
		super.executeExtend(args);
		LOGGER.debug("收到刷新任务列表事件");
	}
	
}
