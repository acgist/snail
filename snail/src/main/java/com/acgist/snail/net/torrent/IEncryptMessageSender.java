package com.acgist.snail.net.torrent;

import java.nio.ByteBuffer;

import com.acgist.snail.config.SystemConfig;
import com.acgist.snail.net.IMessageSender;
import com.acgist.snail.net.NetException;

/**
 * 加密消息代理接口
 * 
 * @author acgist
 */
public interface IEncryptMessageSender extends IMessageSender, IPeerConnect {

    /**
     * 消息加密发送
     * 
     * @param buffer 消息内容
     * 
     * @throws NetException 网络异常
     */
    default void sendEncrypt(ByteBuffer buffer) throws NetException {
        this.sendEncrypt(buffer, SystemConfig.NONE_TIMEOUT);
    }
    
    /**
     * 消息加密发送
     * 
     * @param buffer  消息内容
     * @param timeout 超时时间（秒）
     * 
     * @throws NetException 网络异常
     */
    void sendEncrypt(ByteBuffer buffer, int timeout) throws NetException;
    
}
