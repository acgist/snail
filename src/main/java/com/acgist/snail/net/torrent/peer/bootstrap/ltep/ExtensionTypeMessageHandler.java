package com.acgist.snail.net.torrent.peer.bootstrap.ltep;

import java.nio.ByteBuffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.net.torrent.peer.bootstrap.IExtensionMessageHandler;
import com.acgist.snail.pojo.session.PeerSession;
import com.acgist.snail.system.config.PeerConfig.ExtensionType;
import com.acgist.snail.system.exception.NetException;

/**
 * <p>扩展协议类型抽象</p>
 * 
 * @author acgist
 * @since 1.1.1
 */
public abstract class ExtensionTypeMessageHandler implements IExtensionMessageHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(ExtensionTypeMessageHandler.class);
	
	/**
	 * 扩展协议类型
	 */
	protected final ExtensionType extensionType;
	/**
	 * Peer
	 */
	protected final PeerSession peerSession;
	/**
	 * 扩展协议处理器
	 */
	protected final ExtensionMessageHandler extensionMessageHandler;
	
	public ExtensionTypeMessageHandler(ExtensionType extensionType, PeerSession peerSession, ExtensionMessageHandler extensionMessageHandler) {
		this.extensionType = extensionType;
		this.peerSession = peerSession;
		this.extensionMessageHandler = extensionMessageHandler;
	}

	@Override
	public void onMessage(ByteBuffer buffer) throws NetException {
		if(!this.supportExtensionType()) {
			LOGGER.debug("扩展协议消息错误（不支持扩展协议）：{}", this.extensionType);
			return;
		}
		this.doMessage(buffer);
	}
	
	/**
	 * 消息处理
	 */
	protected abstract void doMessage(ByteBuffer buffer) throws NetException;
	
	/**
	 * 是否支持扩展协议
	 * 
	 * @return true-支持；false-不支持；
	 */
	public boolean supportExtensionType() {
		return this.peerSession.supportExtensionType(this.extensionType);
	}
	
	/**
	 * 获取扩展协议ID
	 * 
	 * @return 扩展协议ID
	 */
	protected Byte extensionTypeId() {
		return this.peerSession.extensionTypeId(this.extensionType);
	}

	/**
	 * 发送扩展消息
	 */
	protected void pushMessage(ByteBuffer buffer) {
		this.pushMessage(buffer.array());
	}
	
	/**
	 * 发送扩展消息
	 */
	protected void pushMessage(byte[] bytes) {
		this.extensionMessageHandler.pushMessage(extensionTypeId(), bytes);
	}
	
}
