package com.acgist.snail.net.peer;

import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PeerServer {

	private static final Logger LOGGER = LoggerFactory.getLogger(PeerServer.class);
	
	private static final String ID_SUFFIX = "Snail-ID-";
	/**
	 * 20位系统ID
	 */
	public static final String ID;
	public static final short PORT = 8888;
	
	static {
		final Random random = new Random();
		final StringBuilder builder = new StringBuilder(ID_SUFFIX);
		final int length = 20 - ID_SUFFIX.length();
		for (int index = 0; index < length; index++) {
			builder.append(random.nextInt(10));
		}
		ID = builder.toString();
		LOGGER.info("系统PeerID：{}，长度：{}", ID, ID.length());
	}
	
}
