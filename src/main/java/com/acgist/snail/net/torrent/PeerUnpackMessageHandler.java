package com.acgist.snail.net.torrent;

import java.nio.ByteBuffer;

import com.acgist.snail.net.torrent.peer.bootstrap.PeerSubMessageHandler;
import com.acgist.snail.system.config.PeerConfig;
import com.acgist.snail.system.config.SystemConfig;
import com.acgist.snail.system.exception.NetException;

/**
 * <p>Peer消息处理：粘包</p>
 * 
 * @author acgist
 * @since 1.1.0
 */
public class PeerUnpackMessageHandler {

	/**
	 * 如果消息长度不够一个Integer长度时使用
	 */
	private static final int INTEGER_BYTE_LENGTH = 4;
	/**
	 * 消息长度
	 */
	private final ByteBuffer lengthStick = ByteBuffer.allocate(INTEGER_BYTE_LENGTH);
	/**
	 * 完整消息
	 */
	private ByteBuffer buffer;
	/**
	 * Peer消息处理器
	 */
	private final PeerSubMessageHandler peerSubMessageHandler;
	
	private PeerUnpackMessageHandler(PeerSubMessageHandler peerSubMessageHandler) {
		this.peerSubMessageHandler = peerSubMessageHandler;
	}
	
	public static final PeerUnpackMessageHandler newInstance(PeerSubMessageHandler peerSubMessageHandler) {
		return new PeerUnpackMessageHandler(peerSubMessageHandler);
	}
	
	/**
	 * 处理Peer消息
	 */
	public void onMessage(ByteBuffer attachment) throws NetException {
		int length = 0;
		while(true) {
			if(this.buffer == null) {
				if(this.peerSubMessageHandler.handshake()) {
					for (int index = 0; index < attachment.limit() && attachment.hasRemaining(); index++) {
						this.lengthStick.put(attachment.get());
						if(this.lengthStick.position() == INTEGER_BYTE_LENGTH) {
							break;
						}
					}
					if(this.lengthStick.position() == INTEGER_BYTE_LENGTH) {
						this.lengthStick.flip();
						length = this.lengthStick.getInt();
						this.lengthStick.compact();
					} else {
						break;
					}
				} else { // 握手
					length = PeerConfig.HANDSHAKE_LENGTH;
				}
				if(length <= 0) { // 心跳
					this.peerSubMessageHandler.keepAlive();
					break;
				}
				if(length >= SystemConfig.MAX_NET_BUFFER_SIZE) {
					throw new NetException("超过最大的网络包大小：" + length);
				}
				this.buffer = ByteBuffer.allocate(length);
			} else {
				length = this.buffer.capacity() - this.buffer.position();
			}
			final int remaining = attachment.remaining();
			if(remaining > length) { // 包含一个完整消息
				final byte[] bytes = new byte[length];
				attachment.get(bytes);
				this.buffer.put(bytes);
				this.peerSubMessageHandler.onMessage(this.buffer);
				this.buffer = null;
			} else if(remaining == length) { // 刚好一个完整消息
				final byte[] bytes = new byte[length];
				attachment.get(bytes);
				this.buffer.put(bytes);
				this.peerSubMessageHandler.onMessage(this.buffer);
				this.buffer = null;
				break;
			} else if(remaining < length) { // 不是完整消息
				final byte[] bytes = new byte[remaining];
				attachment.get(bytes);
				this.buffer.put(bytes);
				break;
			}
		}
	}
	
}
