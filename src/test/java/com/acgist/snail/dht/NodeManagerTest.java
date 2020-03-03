package com.acgist.snail.dht;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.junit.Test;

import com.acgist.snail.BaseTest;
import com.acgist.snail.net.torrent.dht.bootstrap.NodeManager;
import com.acgist.snail.pojo.session.NodeSession;
import com.acgist.snail.utils.ArrayUtils;
import com.acgist.snail.utils.StringUtils;

public class NodeManagerTest extends BaseTest {

	@Test
	public void testCompare() {
		byte[] hex1 = ArrayUtils.xor(StringUtils.unhex("c15419ae6b3bdfd8e983062b0650ad114ce41859"), StringUtils.unhex("c15417e6aeab33732a59085d826edd29978f9afa"));
		byte[] hex2 = ArrayUtils.xor(StringUtils.unhex("c1540515408feb76af06c6c588b1b345b5173c42"), StringUtils.unhex("c15417e6aeab33732a59085d826edd29978f9afa"));
		this.log(StringUtils.hex(hex1));
		this.log(StringUtils.hex(hex2));
		this.log(ArrayUtils.compareUnsigned(hex1, hex2));
	}

	@Test
	public void testFindNode() {
		this.info();
		final List<NodeSession> nodes = new ArrayList<>();
		for (int index = 100000; index < 110000; index++) {
			nodes.add(NodeManager.getInstance().newNodeSession(StringUtils.unhex(buildId()), "0", 0));
		}
		String id = "0000000000000000000000000000000000613709"; // 查找ID
		nodes.add(NodeManager.getInstance().newNodeSession(StringUtils.unhex(id), "0", 0));
		NodeManager.getInstance().sortNodes();
		this.log("查找ID：{}", id);
		this.cost();
		var list = NodeManager.getInstance().findNode(id);
		this.costed();
		list.forEach(node -> {
			this.log(StringUtils.hex(node.getId()));
		});
		id = buildId();
		this.log("查找ID：{}", id);
		this.cost();
		list = NodeManager.getInstance().findNode(id);
		this.costed();
		list.forEach(node -> {
			this.log(StringUtils.hex(node.getId()));
		});
	}

	private String buildId() {
		long value;
		final Random random = new Random();
		while((value = random.nextInt()) < 0) {
		}
		return String.format("%040d", value);
	}
	
}
