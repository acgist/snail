package com.acgist.gui.extend.event;

import com.acgist.snail.gui.event.GuiEvent;
import com.acgist.snail.gui.event.adapter.ShowEventAdapter;

/**
 * <p>GUI显示窗口事件</p>
 * 
 * @author acgist
 * @since 1.4.0
 */
public final class ShowEvent extends ShowEventAdapter {

	private static final ShowEvent INSTANCE = new ShowEvent();
	
	public static final GuiEvent getInstance() {
		return INSTANCE;
	}
	
}
