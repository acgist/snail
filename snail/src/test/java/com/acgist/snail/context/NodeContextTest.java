package com.acgist.snail.context;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import org.junit.jupiter.api.Test;

import com.acgist.snail.config.SystemConfig;
import com.acgist.snail.logger.LoggerConfig;
import com.acgist.snail.pojo.session.NodeSession;
import com.acgist.snail.utils.Performance;
import com.acgist.snail.utils.StringUtils;

public class NodeContextTest extends Performance {

	@Test
	public void testNewNodeSession() {
		LoggerConfig.off();
//		this.costed(1000, () -> {
//			NodeContext.getInstance().newNodeSession(StringUtils.unhex(buildId()), "0", 0);
//		});
//		this.costed(1000, () -> {
//			NodeContext.getInstance().sortNodes();
//		});
		this.costed(10000, () -> {
			NodeContext.getInstance().newNodeSession(StringUtils.unhex(buildId()), "0", 0);
		});
		var oldNodes = NodeContext.getInstance().nodes();
		var newNodes = NodeContext.getInstance().nodes();
		this.log(oldNodes.size());
		this.log(newNodes.size());
		Collections.sort(newNodes);
		assertTrue(oldNodes != newNodes);
		assertEquals(oldNodes.size(), newNodes.size());
//		oldNodes.forEach(node -> this.log(StringUtils.hex(node.getId())));
//		newNodes.forEach(node -> this.log(StringUtils.hex(node.getId())));
		for (int index = 0; index < oldNodes.size(); index++) {
			assertEquals(oldNodes.get(index), newNodes.get(index));
		}
	}

	@Test
	public void testFindNode() {
		LoggerConfig.off();
		this.costed(10000, () -> {
			NodeContext.getInstance().newNodeSession(StringUtils.unhex(buildId()), "0", 0);
		});
		long size = NodeContext.getInstance().nodes().stream().filter(NodeSession::persistentable).count();
		this.log("可用节点：{}", size);
		final var target = buildId();
//		final var target = StringUtils.hex(NodeContext.getInstance().nodes().get(0).getId());
//		NodeContext.getInstance().nodes().forEach(node -> this.log(StringUtils.hex(node.getId())));
//		this.log("----");
		final var nodes = NodeContext.getInstance().findNode(target);
		nodes.forEach(node -> this.log(StringUtils.hex(node.getId())));
//		this.log("----");
		final var newNodes = new ArrayList<>(nodes);
		Collections.sort(newNodes);
//		newNodes.forEach(node -> this.log(StringUtils.hex(node.getId())));
		for (int index = 0; index < nodes.size(); index++) {
			assertEquals(nodes.get(index), newNodes.get(index));
		}
		assertEquals(8, nodes.size());
		this.log(nodes.size());
		this.log(target);
		this.costed(10000, () -> NodeContext.getInstance().findNode(target));
//		this.costed(10000, 10, () -> NodeContext.getInstance().findNode(target));
		size = NodeContext.getInstance().nodes().stream().filter(NodeSession::persistentable).count();
		this.log("可用节点：{}", size);
	}

	@Test
	public void testMinFindNode() {
		LoggerConfig.off();
		this.costed(2, () -> {
			NodeContext.getInstance().newNodeSession(StringUtils.unhex(buildId()), "0", 0);
		});
		final var target = buildId();
		final var nodes = NodeContext.getInstance().findNode(target);
		nodes.forEach(node -> this.log(StringUtils.hex(node.getId())));
		assertTrue(8 > nodes.size());
		this.log(nodes.size());
		this.log(target);
	}

	private String buildId() {
//		long value;
//		final Random random = new Random();
//		while((value = random.nextLong()) < 0);
//		return String.format("%040d", value);
		final byte[] bytes = new byte[20];
		final Random random = new Random();
		for (int index = 0; index < 20; index++) {
			bytes[index] = (byte) random.nextInt(SystemConfig.UNSIGNED_BYTE_MAX);
		}
		return StringUtils.hex(bytes);
	}
	
}
