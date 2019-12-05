package com.acgist.snail.system.initializer.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.net.torrent.peer.PeerServer;
import com.acgist.snail.system.initializer.Initializer;

/**
 * <p>初始化Peer</p>
 * 
 * @author acgist
 * @since 1.0.0
 */
public final class PeerInitializer extends Initializer {

	private static final Logger LOGGER = LoggerFactory.getLogger(PeerInitializer.class);
	
	private PeerInitializer() {
	}
	
	public static final PeerInitializer newInstance() {
		return new PeerInitializer();
	}
	
	@Override
	protected void init() {
		LOGGER.info("初始化Peer");
		PeerServer.getInstance().listen();
	}

}
