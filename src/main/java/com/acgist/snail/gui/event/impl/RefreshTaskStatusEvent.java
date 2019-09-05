package com.acgist.snail.gui.event.impl;

import com.acgist.snail.gui.event.GuiEvent;
import com.acgist.snail.gui.main.TaskDisplay;

/**
 * GUI刷新任务状态事件
 * 
 * @author acgist
 * @since 1.1.0
 */
public class RefreshTaskStatusEvent extends GuiEvent {

	protected RefreshTaskStatusEvent() {
		super(Type.refreshTaskStatus, "刷新任务状态事件");
	}

	@Override
	protected void executeNative(Object ... args) {
		TaskDisplay.getInstance().refreshTaskStatus();
	}

	@Override
	protected void executeExtend(Object ... args) {
		// TODO：外部
	}

	public static final GuiEvent newInstance() {
		return new RefreshTaskStatusEvent();
	}

}
