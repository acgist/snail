package com.acgist.snail.gui;

import com.acgist.snail.format.BEncodeDecoder;
import com.acgist.snail.gui.event.GuiEventMessage;
import com.acgist.snail.net.PacketSizeException;
import com.acgist.snail.net.application.ApplicationMessage;
import com.acgist.snail.net.application.ApplicationMessage.Type;

/**
 * GUI消息
 * 
 * @author acgist
 * 
 * @see Type#ALERT
 * @see Type#NOTICE
 */
public final record GuiMessage(
    /**
     * 消息类型
     */
    GuiContext.MessageType type,
    /**
     * 消息标题
     */
    String title,
    /**
     * 消息内容
     */
    String message
) {
    
    /**
     * 系统消息转换GUI消息
     * 
     * @param message 系统消息
     * 
     * @return GUI消息
     * 
     * @throws PacketSizeException 网络包大小异常
     */
    public static final GuiMessage of(ApplicationMessage message) throws PacketSizeException {
        final String body = message.getBody();
        final BEncodeDecoder decoder = BEncodeDecoder.newInstance(body).next();
        if(decoder.isEmpty()) {
            return null;
        }
        final String type    = decoder.getString(GuiEventMessage.MESSAGE_TYPE);
        final String title   = decoder.getString(GuiEventMessage.MESSAGE_TITLE);
        final String content = decoder.getString(GuiEventMessage.MESSAGE_MESSAGE);
        return new GuiMessage(GuiContext.MessageType.of(type), title, content);
    }
    
}
