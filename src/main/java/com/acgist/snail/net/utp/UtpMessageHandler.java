package com.acgist.snail.net.utp;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.net.UdpMessageHandler;
import com.acgist.snail.net.peer.bootstrap.PeerLauncherMessageHandler;
import com.acgist.snail.net.utp.bootstrap.UtpService;
import com.acgist.snail.system.config.UtpConfig;
import com.acgist.snail.system.exception.NetException;

/**
 * <p>uTorrent transport protocol</p>
 * <p>协议链接：http://www.bittorrent.org/beps/bep_0029.html</p>
 * 
 * @author acgist
 * @since 1.1.0
 */
public class UtpMessageHandler extends UdpMessageHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(UtpMessageHandler.class);
	
	/**
	 * 接收连接ID
	 */
	private short recvId;
	/**
	 * 发送连接ID
	 */
	private short sendId;
	/**
	 * 请求序号
	 */
	private short seqnr;
	
	private final UtpService utpService = UtpService.getInstance();
	
	private InetSocketAddress socketAddress;
	private final PeerLauncherMessageHandler peerLauncherMessageHandler;
	
	/**
	 * 服务端
	 * TODO:map
	 */
	public UtpMessageHandler() {
		this.peerLauncherMessageHandler = PeerLauncherMessageHandler.newInstance();
		this.peerLauncherMessageHandler.messageHandler(this);
	}

	/**
	 * 客户端
	 */
	public UtpMessageHandler(PeerLauncherMessageHandler peerLauncherMessageHandler) {
		this.peerLauncherMessageHandler = peerLauncherMessageHandler;
		this.peerLauncherMessageHandler.messageHandler(this);
		this.recvId = this.utpService.connectionId();
		this.sendId = (short) (this.recvId + 1);
		this.seqnr = 0;
	}

	public void socketAddress(InetSocketAddress socketAddress) {
		this.socketAddress = socketAddress;
	}
	
	public void connectionId(short connectionId) {
		this.sendId = connectionId;
		this.recvId = (short) (this.sendId + 1);
	}
	
	@Override
	public void onMessage(ByteBuffer buffer, InetSocketAddress socketAddress) {
		if(this.socketAddress == null) {
			this.socketAddress = socketAddress;
		}
		final byte type = buffer.get();
		final byte extension = buffer.get();
		final short connectionId = buffer.getShort();
		final int timestamp = buffer.getInt();
		final int timestampDifference = buffer.getInt();
		final short seqnr = buffer.getShort();
		final short acknr = buffer.getShort();
		if(LOGGER.isDebugEnabled()) {
			LOGGER.debug("UTP消息，类型：{}，扩展：{}，连接ID：{}，时间戳：{}，时间戳对比：{}，请求号：{}，应答号：{}",
				type, extension, connectionId, timestamp, timestampDifference, seqnr, acknr);
		}
		
		this.peerLauncherMessageHandler.oneMessage(buffer);
	}

	@Override
	public void send(ByteBuffer buffer) throws NetException {
		send(buffer, this.socketAddress);
	}

	@Override
	public InetSocketAddress remoteSocketAddress() {
		return this.socketAddress;
	}

	/**
	 * 连接
	 */
	public boolean connect() {
		final ByteBuffer buffer = buffer(UtpConfig.SYN, 20);
		buffer.putShort(this.recvId);
		buffer.putInt(timestamp());
		buffer.putInt(0);
		buffer.putInt(0);
		buffer.putShort(this.seqnr);
		buffer.putShort((short) 0);
		return false;
	}

	/**
	 * 应答消息
	 */
	private void state() {
		ByteBuffer buffer = buffer(UtpConfig.STATE, 20);
	}

	/**
	 * 时间戳
	 */
	private int timestamp() {
		return (int) System.nanoTime();
	}
	
	private ByteBuffer buffer(byte type, int size) {
		ByteBuffer buffer = ByteBuffer.allocate(size);
		buffer.put(type);
		buffer.put(UtpConfig.EXTENSION);
		return buffer;
	}
	
	private ByteBuffer packet(ByteBuffer buffer) {
		ByteBuffer packet = ByteBuffer.allocate(1024);
		packet.putInt(0);
		return null;
	}
	
}
