package com.acgist.snail.gui.javafx.event;

import com.acgist.snail.gui.event.GuiEvent;
import com.acgist.snail.gui.event.adapter.ExitEventAdapter;
import com.acgist.snail.gui.javafx.menu.TrayMenu;

import javafx.application.Platform;

/**
 * GUI退出窗口事件
 * 
 * @author acgist
 */
public final class ExitEvent extends ExitEventAdapter {

    private static final ExitEvent INSTANCE = new ExitEvent();
    
    public static final GuiEvent getInstance() {
        return INSTANCE;
    }
    
    private ExitEvent() {
    }

    @Override
    protected void executeNative(Object ... args) {
        // 退出平台
        Platform.exit();
        // 退出托盘
        TrayMenu.exit();
    }

}
