package com.acgist.snail.gui.javafx.event;

import com.acgist.snail.gui.GuiContext;
import com.acgist.snail.gui.event.GuiEvent;
import com.acgist.snail.gui.event.adapter.NoticeEventAdapter;
import com.acgist.snail.gui.javafx.menu.TrayMenu;

/**
 * GUI提示消息事件
 * 
 * @author acgist
 */
public final class NoticeEvent extends NoticeEventAdapter {

    private static final NoticeEvent INSTANCE = new NoticeEvent();
    
    public static final GuiEvent getInstance() {
        return INSTANCE;
    }
    
    private NoticeEvent() {
    }
    
    @Override
    protected void executeNativeExtend(GuiContext.MessageType type, String title, String message) {
        TrayMenu.getInstance().notice(title, message, type);
    }

}
