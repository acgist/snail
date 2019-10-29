package com.acgist.snail.net.torrent.peer.bootstrap.ltep;

import java.nio.ByteBuffer;

import com.acgist.snail.pojo.session.PeerSession;
import com.acgist.snail.system.config.PeerConfig.ExtensionType;

/**
 * <p>完成时发送此消息</p>
 * 
 * @author acgist
 * @since 1.2.0
 */
public class UploadOnlyExtensionMessageHandler extends ExtensionTypeMessageHandler {
	
	private UploadOnlyExtensionMessageHandler(PeerSession peerSession, ExtensionMessageHandler extensionMessageHandler) {
		super(ExtensionType.UPLOAD_ONLY, peerSession, extensionMessageHandler);
	}

	public static final UploadOnlyExtensionMessageHandler newInstance(PeerSession peerSession, ExtensionMessageHandler extensionMessageHandler) {
		return new UploadOnlyExtensionMessageHandler(peerSession, extensionMessageHandler);
	}
	
	@Override
	public void doMessage(ByteBuffer buffer) {
	}

	public void uploadOnly() {
		
	}

}
