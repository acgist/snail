package com.acgist.snail.gui.event.adapter;

import com.acgist.snail.gui.GuiContext;
import com.acgist.snail.gui.event.GuiEvent;

/**
 * <p>GUI退出窗口事件</p>
 * 
 * @author acgist
 */
public class ExitEventAdapter extends GuiEvent {

	public ExitEventAdapter() {
		super(Type.EXIT, "退出窗口事件");
	}

	@Override
	protected void executeNative(Object ... args) {
		this.executeExtend(args);
	}
	
	@Override
	protected void executeExtend(Object ... args) {
		GuiContext.getInstance().unlock();
	}
	
}
