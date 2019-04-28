package com.acgist.snail.net.dht.bootstrap;

import java.util.Random;

import com.acgist.snail.system.config.SystemConfig;

public class NodeService {
	
	private static final NodeService INSTANCE = new NodeService();
	
	private final String nodeId;
	
	private static final String NODE_IDS = SystemConfig.LETTER + SystemConfig.DIGIT;

	private NodeService() {
		this.nodeId = buildNodeId();
	}
	
	public static final NodeService getInstance() {
		return INSTANCE;
	}
	
	/**
	 * 获取当前nodeId
	 */
	public String nodeId() {
		return nodeId;
	}
	
	private String buildNodeId() {
		final StringBuilder builder = new StringBuilder(20);
		final Random random = new Random();
		final int size = NODE_IDS.length();
		for (int i = 0; i < 20; i++) {
			builder.append(NODE_IDS.charAt(random.nextInt(size)));
		}
		return builder.toString();
	}
	
}
