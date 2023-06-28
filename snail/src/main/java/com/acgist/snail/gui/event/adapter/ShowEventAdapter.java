package com.acgist.snail.gui.event.adapter;

import com.acgist.snail.gui.event.GuiEvent;
import com.acgist.snail.net.application.ApplicationMessage;

/**
 * GUI显示窗口事件
 * 
 * @author acgist
 */
public class ShowEventAdapter extends GuiEvent {

    public ShowEventAdapter() {
        super(Type.SHOW, "显示窗口事件");
    }
    
    @Override
    protected void executeNative(Object... args) {
        this.executeExtend(args);
    }

    @Override
    protected void executeExtend(Object ... args) {
        this.sendExtendGuiMessage(ApplicationMessage.Type.SHOW.build());
    }

}
