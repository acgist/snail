package com.acgist.snail.gui.event.adapter;

import com.acgist.snail.gui.event.GuiEventMessage;
import com.acgist.snail.net.application.ApplicationMessage;

/**
 * GUI窗口消息事件
 * 
 * @author acgist
 */
public class AlertEventAdapter extends GuiEventMessage {

    public AlertEventAdapter() {
        super(Type.ALERT, "窗口消息事件", ApplicationMessage.Type.ALERT);
    }

}
