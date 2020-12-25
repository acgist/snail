package com.acgist.snail.gui.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.gui.event.adapter.HideEventAdapter;

/**
 * <p>GUI隐藏窗口事件</p>
 * 
 * @author acgist
 */
public final class HideEvent extends HideEventAdapter {

	private static final Logger LOGGER = LoggerFactory.getLogger(HideEvent.class);
	
	private static final HideEvent INSTANCE = new HideEvent();
	
	public static final GuiEvent getInstance() {
		return INSTANCE;
	}
	
	private HideEvent() {
	}
	
	@Override
	protected void executeExtend(Object... args) {
		super.executeExtend(args);
		LOGGER.debug("收到隐藏窗口事件");
	}

}
