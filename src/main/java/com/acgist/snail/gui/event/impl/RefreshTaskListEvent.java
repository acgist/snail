package com.acgist.snail.gui.event.impl;

import com.acgist.snail.gui.GuiManager;
import com.acgist.snail.gui.event.GuiEvent;
import com.acgist.snail.gui.main.TaskDisplay;
import com.acgist.snail.pojo.message.ApplicationMessage;

/**
 * <p>GUI刷新任务列表事件</p>
 * 
 * @author acgist
 * @since 1.1.0
 */
public final class RefreshTaskListEvent extends GuiEvent {

	private static final RefreshTaskListEvent INSTANCE = new RefreshTaskListEvent();
	
	protected RefreshTaskListEvent() {
		super(Type.REFRESH_TASK_LIST, "刷新任务列表事件");
	}

	public static final GuiEvent getInstance() {
		return INSTANCE;
	}
	
	@Override
	protected void executeNative(Object ... args) {
		TaskDisplay.getInstance().refreshTaskList();
	}

	@Override
	protected void executeExtend(Object ... args) {
		final ApplicationMessage message = ApplicationMessage.message(ApplicationMessage.Type.REFRESH);
		GuiManager.getInstance().sendExtendGuiMessage(message);
	}

}
