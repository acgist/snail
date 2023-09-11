package com.acgist.snail.gui.javafx.event;

import com.acgist.snail.gui.event.GuiEvent;
import com.acgist.snail.gui.event.adapter.ShowEventAdapter;
import com.acgist.snail.gui.javafx.window.main.MainWindow;

import javafx.application.Platform;

/**
 * GUI显示窗口事件
 * 
 * @author acgist
 */
public final class ShowEvent extends ShowEventAdapter {

    private static final ShowEvent INSTANCE = new ShowEvent();
    
    public static final GuiEvent getInstance() {
        return INSTANCE;
    }
    
    private ShowEvent() {
    }
    
    @Override
    protected void executeNative(Object ... args) {
        Platform.runLater(MainWindow.getInstance()::show);
    }

}
