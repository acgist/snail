package com.acgist.snail.dht;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.junit.Test;

import com.acgist.snail.net.torrent.dht.bootstrap.NodeManager;
import com.acgist.snail.pojo.session.NodeSession;
import com.acgist.snail.utils.ArrayUtils;
import com.acgist.snail.utils.StringUtils;

public class NodeManagerTest {

	@Test
	public void compare() {
		byte[] hex1 = NodeManager.xor(StringUtils.unhex("c15419ae6b3bdfd8e983062b0650ad114ce41859"), StringUtils.unhex("c15417e6aeab33732a59085d826edd29978f9afa"));
		byte[] hex2 = NodeManager.xor(StringUtils.unhex("c1540515408feb76af06c6c588b1b345b5173c42"), StringUtils.unhex("c15417e6aeab33732a59085d826edd29978f9afa"));
		System.out.println(StringUtils.hex(hex1));
		System.out.println(StringUtils.hex(hex2));
		System.out.println(ArrayUtils.compareUnsigned(hex1, hex2));
	}

	public String buildId() {
		final Random random = new Random();
		long value;
		while((value = random.nextInt()) < 0) {
		}
		return String.format("%040d", value);
	}
	
	@Test
	public void findNode() {
		List<NodeSession> nodes = new ArrayList<>();
		for (int index = 100000; index < 110000; index++) {
			nodes.add(NodeManager.getInstance().newNodeSession(StringUtils.unhex(buildId()), "0", 0));
		}
		nodes.add(NodeManager.getInstance().newNodeSession(StringUtils.unhex("0000000000000000000000000000000000613709"), "0", 0));
		NodeManager.getInstance().sortNodes();
		String id = "0000000000000000000000000000000000613709";
		System.out.println("id=================>" + id);
		long begin = System.currentTimeMillis();
		var list = NodeManager.getInstance().findNode(id);
		long end = System.currentTimeMillis();
		System.out.println("查询时间：" + (end - begin));
		list.forEach(node -> {
			System.out.println(node);
			System.out.println(StringUtils.hex(node.getId()));
		});
		System.out.println("--------");
		id = buildId();
		System.out.println("id=================>" + id);
		begin = System.currentTimeMillis();
		list = NodeManager.getInstance().findNode(id);
		end = System.currentTimeMillis();
		System.out.println("查询时间：" + (end - begin));
		list.forEach(node -> {
			System.out.println(node);
			System.out.println(StringUtils.hex(node.getId()));
		});
	}

}
