package com.acgist.snail.net.torrent.peer.extension;

import java.nio.ByteBuffer;

import com.acgist.snail.config.PeerConfig;
import com.acgist.snail.config.PeerConfig.ExtensionType;
import com.acgist.snail.logger.Logger;
import com.acgist.snail.logger.LoggerFactory;
import com.acgist.snail.net.torrent.peer.ExtensionMessageHandler;
import com.acgist.snail.net.torrent.peer.ExtensionTypeMessageHandler;
import com.acgist.snail.net.torrent.peer.PeerSession;

/**
 * 下载完成时发送uploadOnly消息
 * 
 * @author acgist
 */
public final class UploadOnlyExtensionMessageHandler extends ExtensionTypeMessageHandler {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(UploadOnlyExtensionMessageHandler.class);
    
    /**
     * 只上传不下载：{@value}
     */
    private static final byte UPLOAD_ONLY = 0x01;
    
    /**
     * @param peerSession             Peer信息
     * @param extensionMessageHandler 扩展消息代理
     */
    private UploadOnlyExtensionMessageHandler(PeerSession peerSession, ExtensionMessageHandler extensionMessageHandler) {
        super(ExtensionType.UPLOAD_ONLY, peerSession, extensionMessageHandler);
    }

    /**
     * 新建uploadOnly扩展协议代理
     * 
     * @param peerSession             Peer信息
     * @param extensionMessageHandler 扩展消息代理
     * 
     * @return uploadOnly扩展协议代理
     */
    public static final UploadOnlyExtensionMessageHandler newInstance(PeerSession peerSession, ExtensionMessageHandler extensionMessageHandler) {
        return new UploadOnlyExtensionMessageHandler(peerSession, extensionMessageHandler);
    }
    
    @Override
    public void doMessage(ByteBuffer buffer) {
        this.uploadOnly(buffer);
    }

    /**
     * 发送uploadOnly消息
     */
    public void uploadOnly() {
        LOGGER.debug("发送uploadOnly消息");
        final byte[] bytes = new byte[] { UPLOAD_ONLY };
        this.pushMessage(bytes);
    }
    
    /**
     * 处理uploadOnly消息
     * 
     * @param buffer 消息
     */
    private void uploadOnly(ByteBuffer buffer) {
        final byte value = buffer.get();
        LOGGER.debug("处理uploadOnly消息：{}", value);
        if(value == UPLOAD_ONLY) {
            this.peerSession.flags(PeerConfig.PEX_UPLOAD_ONLY);
        } else {
            this.peerSession.flagsOff(PeerConfig.PEX_UPLOAD_ONLY);
        }
    }

}
