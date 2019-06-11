package com.acgist.snail.peer;

import java.util.concurrent.Executors;

import org.junit.Test;

import com.acgist.snail.system.manager.PeerManager;
import com.acgist.snail.utils.ThreadUtils;

public class PeerManagerTest {

	@Test
	public void test() {
		PeerManager.getInstance().newPeerSession("1234", null, "192.168.1.1", 19999, (byte) 1);
		PeerManager.getInstance().newPeerSession("1234", null, "192.168.1.2", 19999, (byte) 1);
		PeerManager.getInstance().newPeerSession("1234", null, "192.168.1.3", 19999, (byte) 1);
		PeerManager.getInstance().newPeerSession("1234", null, "192.168.1.4", 19999, (byte) 1);
		PeerManager.getInstance().newPeerSession("1234", null, "192.168.1.5", 19999, (byte) 1);
		var exe = Executors.newCachedThreadPool();
		for (int i = 0; i < 40; i++) {
			exe.submit(() -> {
				var a = PeerManager.getInstance().pick("1234");
				System.out.println(a + "=" + 2);
				PeerManager.getInstance().inferior("1234", a);
			});
		}
		ThreadUtils.sleep(Long.MAX_VALUE);
	}
	
}
