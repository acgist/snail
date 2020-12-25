package com.acgist.snail.gui.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.gui.event.adapter.RefreshTaskStatusEventAdapter;

/**
 * <p>GUI刷新任务状态事件</p>
 * 
 * @author acgist
 */
public final class RefreshTaskStatusEvent extends RefreshTaskStatusEventAdapter {

	private static final Logger LOGGER = LoggerFactory.getLogger(RefreshTaskStatusEvent.class);
	
	private static final RefreshTaskStatusEvent INSTANCE = new RefreshTaskStatusEvent();
	
	public static final GuiEvent getInstance() {
		return INSTANCE;
	}
	
	private RefreshTaskStatusEvent() {
	}

	@Override
	protected void executeExtend(Object... args) {
		super.executeExtend(args);
		LOGGER.debug("收到刷新任务状态事件");
	}
	
}
