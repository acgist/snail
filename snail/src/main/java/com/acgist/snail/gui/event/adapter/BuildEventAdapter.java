package com.acgist.snail.gui.event.adapter;

import com.acgist.snail.gui.GuiContext;
import com.acgist.snail.gui.event.GuiEvent;

/**
 * <p>GUI新建窗口事件</p>
 * 
 * @author acgist
 */
public class BuildEventAdapter extends GuiEvent {
	
	public BuildEventAdapter() {
		super(Type.BUILD, "新建窗口事件");
	}
	
	@Override
	protected void executeNative(Object ... args) {
		this.executeExtend(args);
	}

	@Override
	protected void executeExtend(Object ... args) {
		GuiContext.getInstance().lock();
	}
	
}
