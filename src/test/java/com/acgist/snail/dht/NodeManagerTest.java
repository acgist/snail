package com.acgist.snail.dht;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.acgist.snail.pojo.session.NodeSession;
import com.acgist.snail.system.manager.NodeManager;
import com.acgist.snail.utils.StringUtils;

public class NodeManagerTest {

	@Test
	public void xor() {
		String hex1 = NodeManager.xor(StringUtils.unhex("c15419ae6b3bdfd8e983062b0650ad114ce41859"), StringUtils.unhex("c15417e6aeab33732a59085d826edd29978f9afa"));
		String hex2 = NodeManager.xor(StringUtils.unhex("c1540515408feb76af06c6c588b1b345b5173c42"), StringUtils.unhex("c15417e6aeab33732a59085d826edd29978f9afa"));
		System.out.println(hex1);
		System.out.println(hex2);
	}
	
	@Test
	public void findNode() {
		List<NodeSession> nodes = new ArrayList<>();
		for (int index = 100000; index < 110000; index++) {
			nodes.add(NodeManager.getInstance().newNodeSession(StringUtils.unhex("1111111111111111111111111111111111" + index), "0", 0));
		}
		NodeManager.getInstance().sortNodes();
		System.out.println(nodes.contains(NodeManager.getInstance().newNodeSession(StringUtils.unhex("1111111111111111111111111111111111101111"), "0", 0)));
		var list = NodeManager.getInstance().findNode("1111111111111111111111111111111111112023");
		list.forEach(node -> {
			System.out.println(node);
			System.out.println(StringUtils.hex(node.getId()));
		});
		System.out.println("--------");
		list = NodeManager.getInstance().findNode("1111111111111111111111111111111111112023");
		list.forEach(node -> {
			System.out.println(node);
			System.out.println(StringUtils.hex(node.getId()));
		});
	}

	@Test
	public void clear() {
		int NODE_MAX_SIZE = 1024;
		var nodes = new ArrayList<>();
		for (int i = 0; i < 10000; i++) {
			nodes.add(i);
		}
		var iterator= nodes.iterator();
		final int overSize = nodes.size() - NODE_MAX_SIZE;
		if(overSize > 0) {
			int index = 0;
			final int step = NODE_MAX_SIZE / overSize;
			System.out.println(step);
			if(step > 0) {
				while(iterator.hasNext()) {
					iterator.next();
					if(index++ % step == 0) {
						iterator.remove();
					}
				}
			}
		}
	}
	
}
