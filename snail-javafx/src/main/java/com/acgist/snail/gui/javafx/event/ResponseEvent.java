package com.acgist.snail.gui.javafx.event;

import com.acgist.snail.gui.event.GuiEvent;
import com.acgist.snail.gui.event.adapter.ResponseEventAdapter;
import com.acgist.snail.logger.Logger;
import com.acgist.snail.logger.LoggerFactory;

/**
 * GUI响应消息事件
 * 
 * @author acgist
 */
public final class ResponseEvent extends ResponseEventAdapter {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(ResponseEvent.class);
    
    private static final ResponseEvent INSTANCE = new ResponseEvent();
    
    public static final GuiEvent getInstance() {
        return INSTANCE;
    }
    
    private ResponseEvent() {
    }
    
    @Override
    protected void executeNativeExtend(String message) {
        LOGGER.debug("收到响应消息：{}", message);
    }
    
}
