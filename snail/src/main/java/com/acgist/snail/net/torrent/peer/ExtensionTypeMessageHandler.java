package com.acgist.snail.net.torrent.peer;

import java.nio.ByteBuffer;

import com.acgist.snail.config.PeerConfig.ExtensionType;
import com.acgist.snail.logger.Logger;
import com.acgist.snail.logger.LoggerFactory;
import com.acgist.snail.net.NetException;

/**
 * 扩展协议类型
 * 
 * @author acgist
 */
public abstract class ExtensionTypeMessageHandler implements IExtensionMessageHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExtensionTypeMessageHandler.class);
    
    /**
     * 扩展协议类型
     */
    protected final ExtensionType extensionType;
    /**
     * Peer信息
     */
    protected final PeerSession peerSession;
    /**
     * 扩展协议代理
     */
    protected final ExtensionMessageHandler extensionMessageHandler;
    
    /**
     * @param extensionType           扩展协议类型
     * @param peerSession             Peer信息
     * @param extensionMessageHandler 扩展协议代理
     */
    protected ExtensionTypeMessageHandler(ExtensionType extensionType, PeerSession peerSession, ExtensionMessageHandler extensionMessageHandler) {
        this.extensionType           = extensionType;
        this.peerSession             = peerSession;
        this.extensionMessageHandler = extensionMessageHandler;
    }

    @Override
    public void onMessage(ByteBuffer buffer) throws NetException {
        if(!this.supportExtensionType()) {
            LOGGER.debug("处理扩展协议消息错误（没有支持）：{}", this.extensionType);
            return;
        }
        this.doMessage(buffer);
    }
    
    /**
     * 处理扩展消息
     * 
     * @param buffer 消息
     * 
     * @throws NetException 网络异常
     */
    protected abstract void doMessage(ByteBuffer buffer) throws NetException;
    
    /**
     * 判断是否支持扩展协议
     * 
     * @return 是否支持
     */
    public boolean supportExtensionType() {
        return this.peerSession.supportExtensionType(this.extensionType);
    }
    
    /**
     * @return 扩展协议ID
     */
    protected Byte extensionTypeId() {
        return this.peerSession.extensionTypeId(this.extensionType);
    }

    /**
     * 发送扩展消息
     * 
     * @param buffer 扩展消息
     */
    protected void pushMessage(ByteBuffer buffer) {
        this.pushMessage(buffer.array());
    }
    
    /**
     * 发送扩展消息
     * 
     * @param bytes 扩展消息
     */
    protected void pushMessage(byte[] bytes) {
        this.extensionMessageHandler.pushMessage(this.extensionTypeId(), bytes);
    }
    
}
