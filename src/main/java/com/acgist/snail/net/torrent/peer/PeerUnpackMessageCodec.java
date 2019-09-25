package com.acgist.snail.net.torrent.peer;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

import com.acgist.snail.net.codec.MessageCodec;
import com.acgist.snail.net.torrent.peer.bootstrap.PeerSubMessageHandler;
import com.acgist.snail.system.config.PeerConfig;
import com.acgist.snail.system.config.SystemConfig;
import com.acgist.snail.system.exception.NetException;

/**
 * <p>Peer消息处理：拆包</p>
 * 
 * @author acgist
 * @since 1.1.0
 */
public class PeerUnpackMessageCodec extends MessageCodec<ByteBuffer, ByteBuffer> {

	/**
	 * int字符长度
	 */
	private static final int INT_BYTE_LENGTH = 4;
	/**
	 * 完整消息
	 */
	private ByteBuffer buffer;
	/**
	 * 消息长度
	 */
	private final ByteBuffer lengthStick;
	/**
	 * Peer代理
	 */
	private final PeerSubMessageHandler peerSubMessageHandler;
	
	public PeerUnpackMessageCodec(PeerSubMessageHandler peerSubMessageHandler) {
		super(peerSubMessageHandler);
		this.lengthStick = ByteBuffer.allocate(INT_BYTE_LENGTH);
		this.peerSubMessageHandler = peerSubMessageHandler;
	}
	
	@Override
	public void decode(ByteBuffer buffer, InetSocketAddress address, boolean hasAddress) throws NetException {
		int length = 0;
		while(true) {
			if(this.buffer == null) {
				if(this.peerSubMessageHandler.handshake()) {
					for (int index = 0; index < buffer.limit() && buffer.hasRemaining(); index++) {
						this.lengthStick.put(buffer.get());
						if(this.lengthStick.position() == INT_BYTE_LENGTH) {
							break;
						}
					}
					if(this.lengthStick.position() == INT_BYTE_LENGTH) {
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
			final int remaining = buffer.remaining();
			if(remaining > length) { // 包含一个完整消息
				final byte[] bytes = new byte[length];
				buffer.get(bytes);
				this.buffer.put(bytes);
				this.doNext(this.buffer, address, hasAddress);
				this.buffer = null;
			} else if(remaining == length) { // 刚好一个完整消息
				final byte[] bytes = new byte[length];
				buffer.get(bytes);
				this.buffer.put(bytes);
				this.doNext(this.buffer, address, hasAddress);
				this.buffer = null;
				break;
			} else if(remaining < length) { // 不是完整消息
				final byte[] bytes = new byte[remaining];
				buffer.get(bytes);
				this.buffer.put(bytes);
				break;
			}
		}
	}

}
