package com.acgist.snail.gui.event.adapter;

import com.acgist.snail.gui.event.GuiEvent;

/**
 * <p>GUI隐藏窗口事件</p>
 * 
 * @author acgist
 * @since 1.1.0
 */
public class HideEventAdapter extends GuiEvent {

	protected HideEventAdapter() {
		super(Type.HIDE, "隐藏窗口事件");
	}

	@Override
	protected void executeNative(Object... args) {
		this.executeExtend(args);
	}
	
	@Override
	protected final void executeExtend(Object ... args) {
		// TODO：实现
	}
	
}
