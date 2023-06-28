package com.acgist.snail.gui.event.adapter;

import com.acgist.snail.gui.event.GuiEvent;
import com.acgist.snail.net.application.ApplicationMessage;

/**
 * GUI刷新任务状态事件
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
        this.sendExtendGuiMessage(ApplicationMessage.Type.REFRESH_TASK_STATUS.build());
    }

}
