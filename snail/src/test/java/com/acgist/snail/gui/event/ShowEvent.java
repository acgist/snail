package com.acgist.snail.gui.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.gui.event.adapter.ShowEventAdapter;

/**
 * <p>GUI显示窗口事件</p>
 * 
 * @author acgist
 */
public final class ShowEvent extends ShowEventAdapter {

	private static final Logger LOGGER = LoggerFactory.getLogger(RefreshTaskListEvent.class);
	
	private static final ShowEvent INSTANCE = new ShowEvent();
	
	public static final GuiEvent getInstance() {
		return INSTANCE;
	}
	
	private ShowEvent() {
	}
	
	@Override
	protected void executeExtend(Object... args) {
		super.executeExtend(args);
		LOGGER.debug("收到显示窗口事件");
	}
	
}
