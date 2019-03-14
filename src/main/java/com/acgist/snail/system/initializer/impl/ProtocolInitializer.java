package com.acgist.snail.system.initializer.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.protocol.ProtocolManager;
import com.acgist.snail.protocol.ftp.FtpProtocol;
import com.acgist.snail.protocol.http.HttpProtocol;
import com.acgist.snail.protocol.torrent.TorrentProtocol;
import com.acgist.snail.system.initializer.Initializer;

/**
 * 初始化下载协议
 */
public class ProtocolInitializer extends Initializer {

	private static final Logger LOGGER = LoggerFactory.getLogger(ProtocolInitializer.class);
	
	private ProtocolInitializer() {
	}
	
	public static final ProtocolInitializer newInstance() {
		return new ProtocolInitializer();
	}
	
	@Override
	protected void init() {
		LOGGER.info("初始化下载协议");
		ProtocolManager.getInstance().register(FtpProtocol.getInstance());
		ProtocolManager.getInstance().register(HttpProtocol.getInstance());
		ProtocolManager.getInstance().register(TorrentProtocol.getInstance());
		ProtocolManager.getInstance().available(true);
	}

}
