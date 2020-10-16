package com.acgist.snail.context.initializer.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.context.initializer.Initializer;
import com.acgist.snail.net.torrent.peer.PeerServer;

/**
 * <p>初始化Peer</p>
 * 
 * @author acgist
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
