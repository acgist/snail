package com.acgist.snail.net.utp;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.net.UdpMessageHandler;
import com.acgist.snail.net.peer.bootstrap.PeerLauncherMessageHandler;
import com.acgist.snail.net.utp.bootstrap.UtpService;
import com.acgist.snail.net.utp.bootstrap.UtpWindowHandler;
import com.acgist.snail.system.config.PeerConfig;
import com.acgist.snail.system.config.SystemConfig;
import com.acgist.snail.system.config.UtpConfig;
import com.acgist.snail.system.exception.NetException;
import com.acgist.snail.utils.ThreadUtils;

/**
 * <p>uTorrent transport protocol</p>
 * <p>协议链接：http://www.bittorrent.org/beps/bep_0029.html</p>
 * 
 * @author acgist
 * @since 1.1.0
 */
public class UtpMessageHandler extends UdpMessageHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(UtpMessageHandler.class);
	
	private static final int TIMEOUT = 4000;
	
	/**
	 * 接收连接ID
	 */
	private final short recvId;
	/**
	 * 发送连接ID
	 */
	private final short sendId;
	/**
	 * 请求序号
	 */
	private int seqnr;
	
	/**
	 * 是否连接
	 */
	private boolean connect;
	/**
	 * 连接锁
	 */
	private Object connectLock = new Object();
	
	private ByteBuffer buffer;
	
	private final UtpService utpService = UtpService.getInstance();
	
	private final PeerLauncherMessageHandler peerLauncherMessageHandler;
	
	private final UtpWindowHandler utpWindowHandler = new UtpWindowHandler();
	
	/**
	 * 如果消息长度不够一个Integer长度时使用
	 */
	private static final int INTEGER_BYTE_LENGTH = 4;
	private final ByteBuffer lengthStick = ByteBuffer.allocate(INTEGER_BYTE_LENGTH);
	
	/**
	 * 服务端
	 */
	public UtpMessageHandler(final short connectionId, InetSocketAddress socketAddress) {
		this.peerLauncherMessageHandler = PeerLauncherMessageHandler.newInstance();
		this.peerLauncherMessageHandler.messageHandler(this);
		this.sendId = connectionId;
		this.recvId = (short) (this.sendId + 1);
		this.utpService.putUtpMessageHandler(this.recvId, socketAddress, this);
	}

	/**
	 * 客户端
	 */
	public UtpMessageHandler(PeerLauncherMessageHandler peerLauncherMessageHandler, InetSocketAddress socketAddress) {
		this.peerLauncherMessageHandler = peerLauncherMessageHandler;
		this.peerLauncherMessageHandler.messageHandler(this);
		this.recvId = this.utpService.connectionId();
		this.sendId = (short) (this.recvId + 1); 
		this.seqnr = 0;
		this.utpService.putUtpMessageHandler(this.recvId, socketAddress, this);
	}
	
	@Override
	public void onMessage(ByteBuffer buffer, InetSocketAddress socketAddress) throws NetException {
		if(this.socketAddress == null) {
			this.socketAddress = socketAddress;
		}
		buffer.flip();
		final byte type = buffer.get();
		final byte extension = buffer.get();
		final short connectionId = buffer.getShort();
		final int timestamp = buffer.getInt();
		final int timestampDifference = buffer.getInt();
		final short seqnr = buffer.getShort();
		final short acknr = buffer.getShort();
		if(LOGGER.isDebugEnabled()) {
			LOGGER.debug("UTP消息，类型：{}，扩展：{}，连接ID：{}，时间戳：{}，时间戳对比：{}，请求号：{}，应答号：{}",
				UtpConfig.type(type), extension, connectionId, timestamp, timestampDifference, seqnr, acknr);
		}
		switch (type) {
		case UtpConfig.TYPE_DATA:
			data(timestamp, seqnr, buffer);
			break;
		case UtpConfig.TYPE_FIN:
			fin(timestamp, seqnr);
			break;
		case UtpConfig.TYPE_STATE:
			state(timestamp, seqnr, acknr);
			break;
		case UtpConfig.TYPE_RESET:
			reset(timestamp, seqnr);
			break;
		case UtpConfig.TYPE_SYN:
			syn(timestamp, seqnr);
			break;
		default:
			LOGGER.error("不支持的UTP类型：{}", type);
			return;
		}
	}

	@Override
	public void send(ByteBuffer buffer) throws NetException {
		if(buffer.position() != 0) { //  重置标记
			buffer.flip();
		}
		if(buffer.limit() == 0) {
			LOGGER.warn("发送消息为空");
			return;
		}
		byte[] bytes;
		int remaining;
		while((remaining = buffer.remaining()) > 0) {
			if(remaining > UtpConfig.MAX_PACKET_SIZE) {
				bytes = new byte[UtpConfig.MAX_PACKET_SIZE];
			} else {
				bytes = new byte[remaining];
			}
			buffer.get(bytes);
			this.data(this.utpWindowHandler.timestamp(), this.utpWindowHandler.seqnr(), bytes);
		}
	}

	/**
	 * 连接
	 */
	public boolean connect() {
		this.connect = false;
		this.syn();
		synchronized (this.connectLock) {
			ThreadUtils.wait(this.connectLock, Duration.ofSeconds(TIMEOUT));
		}
		return this.connect;
	}

	/**
	 * 连接成功
	 */
	private void connect(boolean connect) {
		this.connect = connect;
		synchronized (this.connectLock) {
			this.connectLock.notifyAll();
		}
	}

	/**
	 * 接收数据消息
	 */
	private void data(int timestamp, short seqnr, ByteBuffer buffer) throws NetException {
		final ByteBuffer messageBuffer = this.utpWindowHandler.put(timestamp, seqnr, buffer);
		if(messageBuffer == null) {
			this.state(timestamp, seqnr);
			return;
		}
		int length = 0;
		messageBuffer.flip();
		while(true) {
			if(this.buffer == null) {
				if(this.peerLauncherMessageHandler.handshaked()) {
					for (int index = 0; index < messageBuffer.limit(); index++) {
						this.lengthStick.put(messageBuffer.get());
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
			final int remaining = messageBuffer.remaining();
			if(remaining > length) { // 包含一个完整消息
				byte[] bytes = new byte[length];
				messageBuffer.get(bytes);
				this.buffer.put(bytes);
				this.peerLauncherMessageHandler.oneMessage(this.buffer);
				this.buffer = null;
			} else if(remaining == length) { // 刚好一个完整消息
				byte[] bytes = new byte[length];
				messageBuffer.get(bytes);
				this.buffer.put(bytes);
				this.peerLauncherMessageHandler.oneMessage(this.buffer);
				this.buffer = null;
				break;
			} else if(remaining < length) { // 不是完整消息
				byte[] bytes = new byte[remaining];
				messageBuffer.get(bytes);
				this.buffer.put(bytes);
				break;
			}
		}
		this.state(timestamp, seqnr);
	}
	
	/**
	 * 发送数据消息
	 */
	private void data(int timestamp, short seqnr, byte[] bytes) {
		final int now = timestamp();
		final ByteBuffer buffer = header(UtpConfig.TYPE_DATA, bytes.length + 20);
		buffer.putShort(this.sendId);
		buffer.putInt(now);
		buffer.putInt(now - timestamp);
		buffer.putInt(this.utpWindowHandler.remaining());
		buffer.putShort(this.seqnr());
		buffer.putShort(seqnr); // acknr=请求seqnr
		buffer.put(bytes);
		this.pushMessage(buffer);
	}
	
	/**
	 * 接收结束消息
	 */
	private void fin(int timestamp, short seqnr) {
		this.connect = false;
		super.close();
		this.state(timestamp, seqnr);
	}
	
	/**
	 * 发送结束消息
	 */
	private void fin() {
		final ByteBuffer buffer = header(UtpConfig.TYPE_FIN, 20);
		buffer.putShort(this.recvId);
		buffer.putInt(timestamp());
		buffer.putInt(0);
		buffer.putInt(0);
		buffer.putShort(this.seqnr());
		buffer.putShort((short) 0);
		this.pushMessage(buffer);
	}

	/**
	 * 接收应答消息
	 */
	private void state(int timestamp, short seqnr, short acqnr) {
		if(!this.connect) { // 没有连接
			this.connect(this.available());
		} else { // 其他处理
			// TODO：处理
		}
	}
	
	/**
	 * 发送应答消息
	 */
	private void state(int timestamp, short seqnr) {
		final int now = timestamp();
		final ByteBuffer buffer = header(UtpConfig.TYPE_STATE, 20);
		buffer.putShort(this.sendId);
		buffer.putInt(now);
		buffer.putInt(now - timestamp);
		buffer.putInt(this.utpWindowHandler.remaining());
		buffer.putShort((short) (seqnr + 1));
		buffer.putShort(seqnr); // acknr=请求seqnr
		this.pushMessage(buffer);
	}
	
	/**
	 * 接收reset消息
	 */
	private void reset(int timestamp, short seqnr) {
		this.connect = false;
		super.close();
		this.state(timestamp, seqnr);
	}
	
	/**
	 * 发送reset消息
	 */
	private void reset() {
		final ByteBuffer buffer = header(UtpConfig.TYPE_RESET, 20);
		buffer.putShort(this.recvId);
		buffer.putInt(timestamp());
		buffer.putInt(0);
		buffer.putInt(0);
		buffer.putShort(this.seqnr());
		buffer.putShort((short) 0);
		this.pushMessage(buffer);
	}
	
	/**
	 *接收握手消息
	 */
	private void syn(int timestamp, short seqnr) {
		this.state(timestamp, seqnr);
	}
	
	/**
	 * 发送握手消息
	 */
	private void syn() {
		final ByteBuffer buffer = header(UtpConfig.TYPE_SYN, 20);
		buffer.putShort(this.recvId);
		buffer.putInt(timestamp());
		buffer.putInt(0);
		buffer.putInt(0);
		buffer.putShort(this.seqnr());
		buffer.putShort((short) 0);
		this.pushMessage(buffer);
	}

	/**
	 * 时间戳
	 */
	private int timestamp() {
		return (int) System.nanoTime();
	}
	
	/**
	 * 请求号
	 */
	private short seqnr() {
		synchronized (this) {
			return (short) this.seqnr++;
		}
	}
	
	/**
	 * 设置消息头
	 */
	private ByteBuffer header(byte type, int size) {
		final ByteBuffer buffer = ByteBuffer.allocate(size);
		buffer.put(type);
		buffer.put(UtpConfig.EXTENSION);
		return buffer;
	}
	
	/**
	 * 发送消息
	 */
	private void pushMessage(ByteBuffer buffer) {
		try {
			super.send(buffer);
		} catch (NetException e) {
			this.reset();
			LOGGER.error("UTP发送消息异常", e);
		}
	}
	
	/**
	 * 发送fin消息，标记关闭。
	 */
	@Override
	public void close() {
		this.fin();
		super.close();
	}
	
}
