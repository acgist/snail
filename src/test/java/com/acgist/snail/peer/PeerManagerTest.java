package com.acgist.snail.peer;

import java.util.concurrent.Executors;

import org.junit.Test;

import com.acgist.snail.BaseTest;
import com.acgist.snail.net.torrent.peer.bootstrap.PeerManager;
import com.acgist.snail.utils.NetUtils;
import com.acgist.snail.utils.ThreadUtils;

public class PeerManagerTest extends BaseTest {

	@Test
	public void add() {
//		PeerManager.getInstance().newPeerSession("1234", null, "183.6.115.59", 19999, (byte) 1);
//		PeerManager.getInstance().newPeerSession("1234", null, "183.6.115.59", 19999, (byte) 1);
//		PeerManager.getInstance().newPeerSession("1234", null, "183.6.115.59", 19999, (byte) 1);
//		PeerManager.getInstance().newPeerSession("1234", null, "192.168.1.4", 19999, (byte) 1);
//		PeerManager.getInstance().newPeerSession("1234", null, "192.168.1.5", 19999, (byte) 1);
		var exe = Executors.newCachedThreadPool();
		for (int i = 0; i < 40; i++) {
			exe.submit(() -> {
				PeerManager.getInstance().newPeerSession("1234", null, "183.6.115.59", 19999, (byte) 1);
			});
		}
		this.pause();
	}
	
	@Test
	public void thread() {
		String infoHashHex = "1234";
		var exe = Executors.newCachedThreadPool();
		exe.submit(() -> {
			for (int i = 0; i < 4000; i++) {
				try {
					PeerManager.getInstance().newPeerSession(infoHashHex, null, NetUtils.decodeIntToIp(i), 19999, (byte) 1);
					ThreadUtils.sleep(1);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		exe.submit(() -> {
			for (int i = 0; i < 4000; i++) {
				try {
					PeerManager.getInstance().have(infoHashHex, 100);
					ThreadUtils.sleep(1);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		this.pause();
	}
	
}
