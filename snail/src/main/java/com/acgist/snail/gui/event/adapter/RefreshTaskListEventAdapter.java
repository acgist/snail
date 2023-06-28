package com.acgist.snail.gui.event.adapter;

import com.acgist.snail.gui.event.GuiEvent;
import com.acgist.snail.net.application.ApplicationMessage;

/**
 * GUI刷新任务列表事件
 * 
 * @author acgist
 */
public class RefreshTaskListEventAdapter extends GuiEvent {

    public RefreshTaskListEventAdapter() {
        super(Type.REFRESH_TASK_LIST, "刷新任务列表事件");
    }

    @Override
    protected void executeNative(Object... args) {
        this.executeExtend(args);
    }
    
    @Override
    protected void executeExtend(Object ... args) {
        this.sendExtendGuiMessage(ApplicationMessage.Type.REFRESH_TASK_LIST.build());
    }

}
