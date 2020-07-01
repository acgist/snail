package com.acgist.snail.gui.event.adapter;

import com.acgist.snail.gui.event.GuiEvent;

/**
 * <p>GUI显示窗口事件</p>
 * 
 * @author acgist
 * @since 1.1.0
 */
public abstract class ShowEventAdapter extends GuiEvent {

	protected ShowEventAdapter() {
		super(Type.SHOW, "显示窗口事件");
	}

	@Override
	protected final void executeExtend(Object ... args) {
		// TODO：实现
	}

}
