package com.acgist.snail.gui.javafx.event;

import com.acgist.snail.gui.event.GuiEvent;
import com.acgist.snail.gui.event.adapter.BuildEventAdapter;
import com.acgist.snail.gui.javafx.menu.TrayMenu;
import com.acgist.snail.gui.javafx.window.main.MainWindow;

import javafx.application.Platform;

/**
 * GUI新建窗口事件
 * 
 * @author acgist
 */
public final class BuildEvent extends BuildEventAdapter {
    
    private static final BuildEvent INSTANCE = new BuildEvent();
    
    public static final GuiEvent getInstance() {
        return INSTANCE;
    }
    
    private BuildEvent() {
    }
    
    @Override
    protected void executeNative(Object ... args) {
        // 设置是否支持缩放
//      System.setProperty("prism.allowhidpi", "true");
        Platform.startup(() -> {});
        Platform.runLater(() -> {
            TrayMenu.getInstance();
            MainWindow.getInstance().show();
        });
    }

}
