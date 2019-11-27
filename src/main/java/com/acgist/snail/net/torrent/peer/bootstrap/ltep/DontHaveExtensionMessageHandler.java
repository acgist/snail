package com.acgist.snail.net.torrent.peer.bootstrap.ltep;

import java.nio.ByteBuffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.pojo.session.PeerSession;
import com.acgist.snail.system.config.PeerConfig.ExtensionType;
import com.acgist.snail.system.exception.NetException;
import com.acgist.snail.utils.NumberUtils;

/**
 * <p>The lt_donthave extension</p>
 * <p>协议链接：http://bittorrent.org/beps/bep_0054.html</p>
 * 
 * <p>宣布不再含有某个Piece</p>
 * 
 * @author acgist
 * @since 1.2.0
 */
public final class DontHaveExtensionMessageHandler extends ExtensionTypeMessageHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(DontHaveExtensionMessageHandler.class);
	
	private DontHaveExtensionMessageHandler(PeerSession peerSession, ExtensionMessageHandler extensionMessageHandler) {
		super(ExtensionType.LT_DONTHAVE, peerSession, extensionMessageHandler);
	}

	public static final DontHaveExtensionMessageHandler newInstance(PeerSession peerSession, ExtensionMessageHandler extensionMessageHandler) {
		return new DontHaveExtensionMessageHandler(peerSession, extensionMessageHandler);
	}
	
	@Override
	protected void doMessage(ByteBuffer buffer) throws NetException {
		dontHave(buffer);
	}
	
	/**
	 * <p>发送dontHave消息</p>
	 * 
	 * @param index 块索引
	 */
	public void dontHave(int index) {
		LOGGER.debug("发送dontHave消息：{}", index);
		final byte[] bytes = NumberUtils.intToBytes(index);
		this.pushMessage(bytes);
	}
	
	/**
	 * <p>处理dontHave消息</p>
	 */
	private void dontHave(ByteBuffer buffer) {
		final int index = buffer.getInt();
		LOGGER.debug("处理dontHave消息：{}", index);
		this.peerSession.pieceOff(index);
	}

}
