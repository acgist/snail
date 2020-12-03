package com.acgist.snail.gui.extend.event;

import com.acgist.snail.gui.event.GuiEvent;
import com.acgist.snail.gui.event.adapter.HideEventAdapter;

/**
 * <p>GUI隐藏窗口事件</p>
 * 
 * @author acgist
 */
public final class HideEvent extends HideEventAdapter {

	private static final HideEvent INSTANCE = new HideEvent();
	
	public static final GuiEvent getInstance() {
		return INSTANCE;
	}
	
	private HideEvent() {
	}

}
