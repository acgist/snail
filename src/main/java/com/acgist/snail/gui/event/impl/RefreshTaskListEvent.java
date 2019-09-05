package com.acgist.snail.gui.event.impl;

import com.acgist.snail.gui.event.GuiEvent;
import com.acgist.snail.gui.main.TaskDisplay;

/**
 * GUI刷新任务列表事件
 * 
 * @author acgist
 * @since 1.1.0
 */
public class RefreshTaskListEvent extends GuiEvent {

	protected RefreshTaskListEvent() {
		super(Type.refreshTaskList, "刷新任务列表事件");
	}

	@Override
	protected void executeNative(Object ... args) {
		TaskDisplay.getInstance().refreshTaskList();
	}

	@Override
	protected void executeExtend(Object ... args) {
		// TODO：外部
	}

	public static final GuiEvent newInstance() {
		return new RefreshTaskListEvent();
	}
	
}
