package com.acgist.snail.net.peer;

import java.nio.ByteBuffer;

import com.acgist.snail.net.TcpMessageHandler;
import com.acgist.snail.net.peer.bootstrap.PeerLauncherMessageHandler;
import com.acgist.snail.system.config.PeerConfig;
import com.acgist.snail.system.config.SystemConfig;
import com.acgist.snail.system.exception.NetException;

/**
 * <p>Peer消息处理（TCP）</p>
 * 
 * @author acgist
 * @since 1.0.0
 */
public class PeerMessageHandler extends TcpMessageHandler {

//	private static final Logger LOGGER = LoggerFactory.getLogger(PeerMessageHandler.class);
	
	/**
	 * 如果消息长度不够一个Integer长度时使用
	 */
	private static final int INTEGER_BYTE_LENGTH = 4;
	private final ByteBuffer lengthStick = ByteBuffer.allocate(INTEGER_BYTE_LENGTH);
	
	private ByteBuffer buffer;
	
	private PeerLauncherMessageHandler peerLauncherMessageHandler;
	
	public PeerMessageHandler() {
	}

	public PeerMessageHandler(PeerLauncherMessageHandler peerLauncherMessageHandler) {
		this.peerLauncherMessageHandler = peerLauncherMessageHandler;
	}
	
	@Override
	public void onMessage(ByteBuffer attachment) throws NetException {
		int length = 0;
		attachment.flip();
		while(true) {
			if(this.buffer == null) {
				if(this.peerLauncherMessageHandler.handshaked()) {
					for (int index = 0; index < attachment.limit(); index++) {
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
					this.peerLauncherMessageHandler.keepAlive();
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
				byte[] bytes = new byte[length];
				attachment.get(bytes);
				this.buffer.put(bytes);
				this.peerLauncherMessageHandler.oneMessage(this.buffer);
				this.buffer = null;
			} else if(remaining == length) { // 刚好一个完整消息
				byte[] bytes = new byte[length];
				attachment.get(bytes);
				this.buffer.put(bytes);
				this.peerLauncherMessageHandler.oneMessage(this.buffer);
				this.buffer = null;
				break;
			} else if(remaining < length) { // 不是完整消息
				byte[] bytes = new byte[remaining];
				attachment.get(bytes);
				this.buffer.put(bytes);
				break;
			}
		}
	}

}
