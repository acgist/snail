package com.acgist.snail.net.torrent.peer.bootstrap.ltep;

import java.nio.ByteBuffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.pojo.session.PeerSession;
import com.acgist.snail.system.config.PeerConfig;
import com.acgist.snail.system.config.PeerConfig.ExtensionType;

/**
 * <p>下载完成时发送uploadOnly消息</p>
 * 
 * @author acgist
 * @since 1.2.0
 */
public final class UploadOnlyExtensionMessageHandler extends ExtensionTypeMessageHandler {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(UploadOnlyExtensionMessageHandler.class);
	
	/**
	 * <p>只上传不下载：{@value}</p>
	 */
	private static final byte UPLOAD_ONLY = 0x01;
	
	private UploadOnlyExtensionMessageHandler(PeerSession peerSession, ExtensionMessageHandler extensionMessageHandler) {
		super(ExtensionType.UPLOAD_ONLY, peerSession, extensionMessageHandler);
	}

	/**
	 * <p>创建uploadOnly消息代理</p>
	 * 
	 * @param peerSession Peer
	 * @param extensionMessageHandler 扩展消息代理
	 * 
	 * @return uploadOnly消息代理
	 */
	public static final UploadOnlyExtensionMessageHandler newInstance(PeerSession peerSession, ExtensionMessageHandler extensionMessageHandler) {
		return new UploadOnlyExtensionMessageHandler(peerSession, extensionMessageHandler);
	}
	
	@Override
	public void doMessage(ByteBuffer buffer) {
		this.uploadOnly(buffer);
	}

	/**
	 * <p>发送uploadOnly消息</p>
	 */
	public void uploadOnly() {
		LOGGER.debug("发送uploadOnly消息");
		final byte[] bytes = new byte[1];
		bytes[0] = UPLOAD_ONLY; // 只上传不下载
		this.pushMessage(bytes);
	}
	
	/**
	 * <p>处理uploadOnly消息</p>
	 * 
	 * @param buffer 消息
	 */
	private void uploadOnly(ByteBuffer buffer) {
		if(!buffer.hasRemaining()) {
			LOGGER.debug("处理uploadOnly消息错误（长度）：{}", buffer.remaining());
			return;
		}
		final byte value = buffer.get();
		LOGGER.debug("处理uploadOnly消息：{}", value);
		if(value == UPLOAD_ONLY) {
			this.peerSession.flags(PeerConfig.PEX_UPLOAD_ONLY);
		} else {
			this.peerSession.flagsOff(PeerConfig.PEX_UPLOAD_ONLY);
		}
	}

}
