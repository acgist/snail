package com.acgist.snail.peer;

import org.junit.Test;

import com.acgist.snail.BaseTest;
import com.acgist.snail.pojo.session.PeerSession;
import com.acgist.snail.pojo.session.StatisticsSession;

public class PeerSessionTest extends BaseTest {

	@Test
	public void testMemory() {
		PeerSession peerSession = PeerSession.newInstance(new StatisticsSession(), "192.168.1.1", 20000);
		PeerSession pexSource = PeerSession.newInstance(new StatisticsSession(), "192.168.1.1", 20000);
		peerSession.pexSource(pexSource);
		this.log(peerSession);
		this.pause();
	}
	
}
