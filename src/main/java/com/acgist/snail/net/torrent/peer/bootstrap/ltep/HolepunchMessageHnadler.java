package com.acgist.snail.net.torrent.peer.bootstrap.ltep;

import java.nio.ByteBuffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.net.torrent.peer.bootstrap.IExtensionMessageHandler;
import com.acgist.snail.pojo.session.PeerSession;
import com.acgist.snail.system.config.PeerConfig.ExtensionType;
import com.acgist.snail.system.config.PeerConfig.HolepunchErrorCode;
import com.acgist.snail.system.config.PeerConfig.HolepunchType;
import com.acgist.snail.utils.NetUtils;

/**
 * <p>Holepunch extension</p>
 * <p>协议链接：http://www.bittorrent.org/beps/bep_0055.html</p>
 * 
 * @author acgist
 * @since 1.1.0
 */
public class HolepunchMessageHnadler implements IExtensionMessageHandler {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(HolepunchMessageHnadler.class);

	private final PeerSession peerSession;
	
	private final ExtensionMessageHandler extensionMessageHandler;
	
	private HolepunchMessageHnadler(PeerSession peerSession, ExtensionMessageHandler extensionMessageHandler) {
		this.peerSession = peerSession;
		this.extensionMessageHandler = extensionMessageHandler;
	}
	
	public static final HolepunchMessageHnadler newInstance(PeerSession peerSession, ExtensionMessageHandler extensionMessageHandler) {
		return new HolepunchMessageHnadler(peerSession, extensionMessageHandler);
	}

	@Override
	public void onMessage(ByteBuffer buffer) {
		
	}
	
	/**
	 * 客户端的消息类型
	 */
	private Byte holepunchType() {
		return this.peerSession.extensionTypeValue(ExtensionType.ut_holepunch);
	}
	
	/**
	 * 消息：TODO：IPv6
	 */
	private ByteBuffer buildMessage(HolepunchType type, String ip, int port, HolepunchErrorCode errorCode) {
		final ByteBuffer buffer = ByteBuffer.allocate(12);
		buffer.put(type.value()); // 消息类型
		buffer.put((byte) 0x00); // 地址类型：0x00=IPv4；0x01=IPv6；
		buffer.putInt(NetUtils.encodeIpToInt(ip)); // IP地址
		buffer.putShort(NetUtils.encodePort(port)); // 端口号
		buffer.putInt(errorCode.code()); // 错误代码
		return buffer;
	}
	
	/**
	 * 发送消息
	 */
	private void pushMessage(ByteBuffer buffer) {
		final Byte type = holepunchType(); // 扩展消息类型
		if (type == null) {
			LOGGER.warn("不支持holepunch扩展协议");
			return;
		}
		this.extensionMessageHandler.pushMessage(type, buffer.array());
	}
	
}
