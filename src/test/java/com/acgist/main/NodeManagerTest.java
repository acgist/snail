package com.acgist.main;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.acgist.snail.pojo.session.NodeSession;
import com.acgist.snail.system.manager.NodeManager;
import com.acgist.snail.utils.StringUtils;

public class NodeManagerTest {

	@Test
	public void findNode() {
		List<NodeSession> nodes = new ArrayList<>();
		for (int index = 100000; index < 110000; index++) {
			nodes.add(NodeSession.newInstance(StringUtils.unhex("1111111111111111111111111111111111" + index), "0", 0));
		}
		System.out.println(nodes.contains(NodeSession.newInstance(StringUtils.unhex("1111111111111111111111111111111111102022"), "0", 0)));
		NodeManager.getInstance().put(nodes);
		var list = NodeManager.getInstance().findNode("1111111111111111111111111111111111112023");
		list.forEach(node -> {
			System.out.println(node);
		});
	}
	
}
