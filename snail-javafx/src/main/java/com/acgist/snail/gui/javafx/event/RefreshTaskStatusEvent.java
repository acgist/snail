package com.acgist.snail.gui.javafx.event;

import com.acgist.snail.gui.event.GuiEvent;
import com.acgist.snail.gui.event.adapter.RefreshTaskStatusEventAdapter;
import com.acgist.snail.gui.javafx.window.main.TaskDisplay;

/**
 * GUI刷新任务状态事件
 * 
 * @author acgist
 */
public final class RefreshTaskStatusEvent extends RefreshTaskStatusEventAdapter {

    private static final RefreshTaskStatusEvent INSTANCE = new RefreshTaskStatusEvent();
    
    public static final GuiEvent getInstance() {
        return INSTANCE;
    }
    
    private RefreshTaskStatusEvent() {
    }
    
    @Override
    protected void executeNative(Object ... args) {
        // 不用使用：Platform.runLater
        TaskDisplay.getInstance().refreshTaskStatus();
    }

}
