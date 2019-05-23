package com.acgist.snail.net.utp;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

import com.acgist.snail.net.UdpMessageHandler;
import com.acgist.snail.net.peer.bootstrap.PeerLauncherMessageHandler;
import com.acgist.snail.system.exception.NetException;
import com.acgist.snail.system.interfaces.IPeerMessageHandler;

/**
 * <p>uTorrent transport protocol</p>
 * 
 * @author acgist
 * @since 1.1.0
 */
public class UtpMessageHandler extends UdpMessageHandler implements IPeerMessageHandler {

	private InetSocketAddress socketAddress;
	private final PeerLauncherMessageHandler peerLauncherMessageHandler;
	
	/**
	 * 服务端
	 */
	public UtpMessageHandler() {
		this.peerLauncherMessageHandler = PeerLauncherMessageHandler.newInstance();
		this.peerLauncherMessageHandler.peerMessageHandler(this);
	}

	/**
	 * 客户端
	 */
	public UtpMessageHandler(PeerLauncherMessageHandler peerLauncherMessageHandler) {
		this.peerLauncherMessageHandler = peerLauncherMessageHandler;
		this.peerLauncherMessageHandler.peerMessageHandler(this);
	}

	public void socketAddress(InetSocketAddress socketAddress) {
		this.socketAddress = socketAddress;
	}
	
	@Override
	public void onMessage(ByteBuffer buffer, InetSocketAddress socketAddress) {
		if(this.socketAddress == null) {
			this.socketAddress = socketAddress;
		}
		// TODO:
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
		return false;
	}
	
}
