package com.acgist.snail.gui.javafx.event;

import com.acgist.snail.gui.event.GuiEvent;
import com.acgist.snail.gui.event.adapter.HideEventAdapter;
import com.acgist.snail.gui.javafx.window.main.MainWindow;

import javafx.application.Platform;

/**
 * GUI隐藏窗口事件
 * 
 * @author acgist
 */
public final class HideEvent extends HideEventAdapter {

    private static final HideEvent INSTANCE = new HideEvent();
    
    public static final GuiEvent getInstance() {
        return INSTANCE;
    }
    
    private HideEvent() {
    }

    @Override
    protected void executeNative(Object ... args) {
        Platform.runLater(MainWindow.getInstance()::hide);
    }

}
