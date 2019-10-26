package com.acgist.snail.gui.event.impl;

import com.acgist.snail.gui.GuiHandler;
import com.acgist.snail.gui.event.GuiEvent;
import com.acgist.snail.gui.main.TaskDisplay;
import com.acgist.snail.pojo.message.ApplicationMessage;

/**
 * GUI刷新任务状态事件
 * 
 * @author acgist
 * @since 1.1.0
 */
public class RefreshTaskStatusEvent extends GuiEvent {

	private static final RefreshTaskStatusEvent INSTANCE = new RefreshTaskStatusEvent();
	
	protected RefreshTaskStatusEvent() {
		super(Type.REFRESH_TASK_STATUS, "刷新任务状态事件");
	}

	public static final GuiEvent getInstance() {
		return INSTANCE;
	}
	
	@Override
	protected void executeNative(Object ... args) {
		TaskDisplay.getInstance().refreshTaskStatus();
	}

	@Override
	protected void executeExtend(Object ... args) {
		final ApplicationMessage message = ApplicationMessage.message(ApplicationMessage.Type.refresh);
		GuiHandler.getInstance().sendGuiMessage(message);
	}

}
