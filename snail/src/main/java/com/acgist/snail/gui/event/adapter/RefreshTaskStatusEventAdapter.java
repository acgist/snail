package com.acgist.snail.gui.event.adapter;

import com.acgist.snail.gui.GuiContext;
import com.acgist.snail.gui.event.GuiEvent;
import com.acgist.snail.net.application.ApplicationMessage;

/**
 * <p>GUI刷新任务状态事件</p>
 * 
 * @author acgist
 */
public class RefreshTaskStatusEventAdapter extends GuiEvent {

	public RefreshTaskStatusEventAdapter() {
		super(Type.REFRESH_TASK_STATUS, "刷新任务状态事件");
	}

	@Override
	protected void executeNative(Object... args) {
		this.executeExtend(args);
	}
	
	@Override
	protected void executeExtend(Object ... args) {
		final ApplicationMessage message = ApplicationMessage.Type.REFRESH_TASK_STATUS.build();
		GuiContext.getInstance().sendExtendGuiMessage(message);
	}

}
