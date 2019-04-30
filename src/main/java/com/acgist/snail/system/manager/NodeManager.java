package com.acgist.snail.system.manager;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.pojo.session.NodeSession;
import com.acgist.snail.system.config.SystemConfig;
import com.acgist.snail.utils.CollectionUtils;

/**
 * Node信息
 */
public class NodeManager {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(NodeManager.class);
	
	/**
	 * Node最大数量，超过这个数量会均匀剔除多余Node
	 */
	private static final int MAX_NODES = 10240;
	
	private final byte[] nodeId;
	private final List<NodeSession> nodes;
	
	private NodeManager() {
		this.nodeId = buildNodeId();
		this.nodes = new ArrayList<>();
	}
	
	private static final NodeManager INSTANCE = new NodeManager();
	
	public static final NodeManager getInstance() {
		return INSTANCE;
	}
	
	/**
	 * 获取当前nodeId
	 */
	public byte[] nodeId() {
		return nodeId;
	}
	
	private byte[] buildNodeId() {
		final byte[] bytes = new byte[20];
		final Random random = new Random();
		for (int i = 0; i < 20; i++) {
			bytes[i] = (byte) random.nextInt(SystemConfig.UNSIGNED_BYTE_SIZE);
		}
		return bytes;
	}
	
	public void put(List<NodeSession> nodes) {
		if(CollectionUtils.isEmpty(nodes)) {
			return;
		}
		synchronized (this.nodes) {
			nodes.stream()
			.filter(node -> !this.nodes.contains(node))
			.forEach(node -> {
				if(LOGGER.isDebugEnabled()) {
					LOGGER.debug("添加Node：{}-{}:{}", node.getIdHex(), node.getHost(), node.getPort());
				}
				this.nodes.add(node);
			});
		}
	}

}
