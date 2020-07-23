package com.acgist.gui.extend.event;

import com.acgist.snail.gui.event.GuiEvent;
import com.acgist.snail.gui.event.adapter.HideEventAdapter;

/**
 * <p>GUI隐藏窗口事件</p>
 * 
 * @author acgist
 * @since 1.4.0
 */
public final class HideEvent extends HideEventAdapter {

	private static final HideEvent INSTANCE = new HideEvent();
	
	public static final GuiEvent getInstance() {
		return INSTANCE;
	}
	
	private HideEvent() {
	}

}
