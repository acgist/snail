package com.acgist.gui.extend.event;

import com.acgist.snail.gui.event.GuiEvent;
import com.acgist.snail.gui.event.adapter.NoticeEventAdapter;

/**
 * <p>GUI提示消息事件</p>
 * 
 * @author acgist
 * @since 1.1.0
 */
public final class NoticeEvent extends NoticeEventAdapter {

	private static final NoticeEvent INSTANCE = new NoticeEvent();
	
	public static final GuiEvent getInstance() {
		return INSTANCE;
	}
	
}
