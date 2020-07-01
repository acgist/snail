package com.acgist.snail.gui.event.adapter;

import com.acgist.snail.gui.GuiManager;
import com.acgist.snail.gui.event.GuiEvent;
import com.acgist.snail.pojo.message.ApplicationMessage;

/**
 * <p>GUI刷新任务列表事件</p>
 * 
 * @author acgist
 * @since 1.1.0
 */
public abstract class RefreshTaskListEventAdapter extends GuiEvent {

	protected RefreshTaskListEventAdapter() {
		super(Type.REFRESH_TASK_LIST, "刷新任务列表事件");
	}

	@Override
	protected final void executeExtend(Object ... args) {
		final ApplicationMessage message = ApplicationMessage.message(ApplicationMessage.Type.REFRESH);
		GuiManager.getInstance().sendExtendGuiMessage(message);
	}

}
