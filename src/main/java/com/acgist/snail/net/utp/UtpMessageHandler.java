package com.acgist.snail.net.utp;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.net.UdpMessageHandler;
import com.acgist.snail.net.peer.bootstrap.PeerLauncherMessageHandler;
import com.acgist.snail.net.utp.bootstrap.UtpService;
import com.acgist.snail.net.utp.bootstrap.UtpWindowData;
import com.acgist.snail.net.utp.bootstrap.UtpWindowHandler;
import com.acgist.snail.system.config.PeerConfig;
import com.acgist.snail.system.config.SystemConfig;
import com.acgist.snail.system.config.UtpConfig;
import com.acgist.snail.system.exception.NetException;
import com.acgist.snail.utils.ThreadUtils;

/**
 * <p>uTorrent transport protocol</p>
 * <p>协议链接：http://www.bittorrent.org/beps/bep_0029.html</p>
 * 流量控制：
 * 阻塞控制：
 * 
 * @author acgist
 * @since 1.1.0
 */
public class UtpMessageHandler extends UdpMessageHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(UtpMessageHandler.class);
	
	/**
	 * 限速等待（秒）
	 */
	private static final int LIMIT_WAIT = 2;
	/**
	 * 连接超时时间（毫秒）
	 */
	private static final int CONNECT_TIMEOUT = 4000;
	
	/**
	 * 接收连接ID
	 */
	private final short recvId;
	/**
	 * 发送连接ID
	 */
	private final short sendId;
	/**
	 * 是否连接
	 */
	private boolean connect;
	/**
	 * 连接锁
	 */
	private Object connectLock = new Object();
	
	private ByteBuffer buffer;
	/**
	 * 发送窗口
	 */
	private final UtpWindowHandler sendWindowHandler;
	/**
	 * 接收窗口
	 */
	private final UtpWindowHandler receiveWindowHandler;
	
	private final UtpService utpService = UtpService.getInstance();
	
	private final PeerLauncherMessageHandler peerLauncherMessageHandler;
	
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
		this.sendWindowHandler = UtpWindowHandler.newInstance();
		this.receiveWindowHandler = UtpWindowHandler.newInstance();
		this.socketAddress = socketAddress;
		this.sendId = connectionId;
		this.recvId = (short) (this.sendId + 1);
		this.utpService.put(this);
	}

	/**
	 * 客户端
	 */
	public UtpMessageHandler(PeerLauncherMessageHandler peerLauncherMessageHandler, InetSocketAddress socketAddress) {
		this.peerLauncherMessageHandler = peerLauncherMessageHandler;
		this.peerLauncherMessageHandler.messageHandler(this);
		this.sendWindowHandler = UtpWindowHandler.newInstance();
		this.receiveWindowHandler = UtpWindowHandler.newInstance();
		this.socketAddress = socketAddress;
		this.recvId = this.utpService.connectionId();
		this.sendId = (short) (this.recvId + 1);
		this.utpService.put(this);
	}
	
	/**
	 * 外网连入时key=地址+connectionId，本机key=connectionId
	 */
	public String key() {
		return this.utpService.buildKey(this.recvId, this.socketAddress);
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
		final int wndSize = buffer.getInt();
		final short seqnr = buffer.getShort();
		final short acknr = buffer.getShort();
		switch (type) {
		case UtpConfig.TYPE_DATA:
			data(timestamp, seqnr, acknr, buffer);
			break;
		case UtpConfig.TYPE_FIN:
			fin(timestamp, seqnr, acknr);
			break;
		case UtpConfig.TYPE_STATE:
			state(timestamp, seqnr, acknr, wndSize);
			break;
		case UtpConfig.TYPE_RESET:
			reset(timestamp, seqnr, acknr);
			break;
		case UtpConfig.TYPE_SYN:
			syn(timestamp, seqnr, acknr);
			break;
		default:
			LOGGER.error(
				"UTP消息，类型：{}，扩展：{}，连接ID：{}，时间戳：{}，时间戳对比：{}，窗口大小：{}，请求号：{}，应答号：{}",
				type, extension, connectionId, timestamp, timestampDifference, wndSize, seqnr, acknr);
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
			final List<UtpWindowData> windowDatas = this.sendWindowHandler.send(bytes);
			this.data(windowDatas);
		}
		Thread.yield(); // 让出CPU时间，防止速度太快导致假死。
		sendLimit();
	}

	/**
	 * 流量控制和阻塞控制。
	 */
	public void sendLimit() {
		while(this.sendWindowHandler.sendLimit()) {
			timeoutRetry();
			synchronized (this.sendWindowHandler) {
				ThreadUtils.wait(this.sendWindowHandler, Duration.ofSeconds(LIMIT_WAIT));
			}
		}
	}
	
	/**
	 * 重新发送超时数据包。
	 */
	private void timeoutRetry() {
		final List<UtpWindowData> windowDatas = this.sendWindowHandler.timeoutRetry();
		if(!windowDatas.isEmpty()) {
			LOGGER.debug("重试未发送数据：{}", windowDatas.size());
			data(windowDatas);
		}
	}
	
	/**
	 * 连接
	 */
	public boolean connect() {
		this.connect = false;
		this.syn();
		synchronized (this.connectLock) {
			ThreadUtils.wait(this.connectLock, Duration.ofSeconds(CONNECT_TIMEOUT));
		}
		return this.connect;
	}

	/**
	 * 接收数据消息
	 */
	private void data(int timestamp, short seqnr, short acknr, ByteBuffer buffer) throws NetException {
		UtpWindowData windowData = null;
		try {
			windowData = this.receiveWindowHandler.receive(timestamp, seqnr, buffer);
		} catch (NetException e) {
			this.resetAndClose();
			throw e;
		} catch (Exception e) {
			this.resetAndClose();
			throw new NetException(e);
		}
		if(windowData == null) {
			return;
		}
		int length = 0;
		final ByteBuffer windowBuffer = windowData.buffer();
		while(true) {
			if(this.buffer == null) {
				if(this.peerLauncherMessageHandler.handshaked()) {
					for (int index = 0; index < windowBuffer.limit(); index++) {
						this.lengthStick.put(windowBuffer.get());
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
			final int remaining = windowBuffer.remaining();
			if(remaining > length) { // 包含一个完整消息
				byte[] bytes = new byte[length];
				windowBuffer.get(bytes);
				this.buffer.put(bytes);
				this.peerLauncherMessageHandler.oneMessage(this.buffer);
				this.buffer = null;
			} else if(remaining == length) { // 刚好一个完整消息
				byte[] bytes = new byte[length];
				windowBuffer.get(bytes);
				this.buffer.put(bytes);
				this.peerLauncherMessageHandler.oneMessage(this.buffer);
				this.buffer = null;
				break;
			} else if(remaining < length) { // 不是完整消息
				byte[] bytes = new byte[remaining];
				windowBuffer.get(bytes);
				this.buffer.put(bytes);
				break;
			}
		}
		this.state(windowData.getTimestamp(), windowData.getSeqnr());
	}
	
	/**
	 * 发送数据消息
	 */
	private void data(List<UtpWindowData> windowDatas) {
		windowDatas.forEach(windowData -> {
			final ByteBuffer buffer = header(UtpConfig.TYPE_DATA, windowData.getLength() + 20);
			buffer.putShort(this.sendId);
			buffer.putInt(windowData.getTimestamp());
			buffer.putInt(windowData.getTimestamp() - this.receiveWindowHandler.lastTimestamp());
			buffer.putInt(this.receiveWindowHandler.wndSize());
			buffer.putShort(windowData.getSeqnr());
			buffer.putShort(this.receiveWindowHandler.lastSeqnr()); // acknr=请求seqnr
			buffer.put(windowData.getData());
			this.pushMessage(buffer);
		});
	}
	
	/**
	 * 接收结束消息
	 */
	private void fin(int timestamp, short seqnr, short acknr) {
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
		buffer.putInt(UtpWindowHandler.timestamp());
		buffer.putInt(0);
		buffer.putInt(0);
		buffer.putShort((short) (this.sendWindowHandler.lastSeqnr() + 1));
		buffer.putShort((short) 0);
		this.pushMessage(buffer);
	}

	/**
	 * 接收应答消息
	 */
	private void state(int timestamp, short seqnr, short acknr, int wndSize) {
		if(!this.connect) { // 没有连接
			this.connect = this.available();
			if(this.connect) {
				this.receiveWindowHandler.connect(timestamp, (short) (seqnr - 1)); // 收到响应时seqnr+1，当前-1。
			}
			synchronized (this.connectLock) {
				this.connectLock.notifyAll();
			}
		}
		this.sendWindowHandler.ack(acknr, wndSize);
	}
	
	/**
	 * 发送应答消息，发送此消息不增加seqnr。
	 */
	private void state(int timestamp, short seqnr) {
		final int now = UtpWindowHandler.timestamp();
		final ByteBuffer buffer = header(UtpConfig.TYPE_STATE, 20);
		buffer.putShort(this.sendId);
		buffer.putInt(now);
		buffer.putInt(now - timestamp);
		buffer.putInt(this.receiveWindowHandler.wndSize());
		buffer.putShort(this.sendWindowHandler.lastSeqnr());
		buffer.putShort(seqnr); // acknr=请求seqnr
		this.pushMessage(buffer);
	}
	
	/**
	 * 接收reset消息
	 */
	private void reset(int timestamp, short seqnr, short acknr) {
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
		buffer.putInt(UtpWindowHandler.timestamp());
		buffer.putInt(0);
		buffer.putInt(0);
		buffer.putShort((short) (this.sendWindowHandler.lastSeqnr() + 1));
		buffer.putShort((short) 0);
		this.pushMessage(buffer);
	}
	
	/**
	 *接收握手消息
	 */
	private void syn(int timestamp, short seqnr, short acknr) {
		if(!this.connect) {
			this.connect = true;
			this.receiveWindowHandler.connect(timestamp, seqnr);
		}
		this.state(timestamp, seqnr);
	}
	
	/**
	 * 发送握手消息，第一条消息。
	 */
	private void syn() {
		final UtpWindowData windowData = this.sendWindowHandler.send(null).get(0);
		final ByteBuffer buffer = header(UtpConfig.TYPE_SYN, 20);
		buffer.putShort(this.recvId);
		buffer.putInt(windowData.getTimestamp());
		buffer.putInt(0);
		buffer.putInt(0);
		buffer.putShort(windowData.getSeqnr());
		buffer.putShort((short) 0);
		this.pushMessage(buffer);
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
			LOGGER.error("UTP发送消息异常", e);
		}
	}
	
	/**
	 * 发送fin消息，标记关闭。
	 */
	@Override
	public void close() {
		this.utpService.remove(this);
		this.fin();
		super.close();
	}
	
	private void resetAndClose() {
		this.utpService.remove(this);
		this.reset();
		super.close();
	}
	
}
