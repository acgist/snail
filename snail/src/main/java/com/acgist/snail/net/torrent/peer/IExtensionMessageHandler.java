package com.acgist.snail.net.torrent.peer;

import java.nio.ByteBuffer;

import com.acgist.snail.net.NetException;

/**
 * 扩展协议接口
 * 
 * @author acgist
 */
public interface IExtensionMessageHandler {

    /**
     * 处理扩展消息
     * 
     * @param buffer 消息
     * 
     * @throws NetException 网络异常
     */
    void onMessage(ByteBuffer buffer) throws NetException;
    
}
