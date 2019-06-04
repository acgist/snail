package com.acgist.snail.system.initializer.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.net.bt.peer.PeerServer;
import com.acgist.snail.system.initializer.Initializer;

/**
 * Peer初始化
 */
public class PeerInitializer extends Initializer {

	private static final Logger LOGGER = LoggerFactory.getLogger(PeerInitializer.class);
	
	private PeerInitializer() {
	}
	
	public static final PeerInitializer newInstance() {
		return new PeerInitializer();
	}
	
	@Override
	protected void init() {
		LOGGER.info("初始化Peer Server");
		final PeerServer server = PeerServer.getInstance();
		server.listen();
	}

}
