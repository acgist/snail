package com.acgist.snail.peer;

import org.junit.Test;

import com.acgist.snail.pojo.session.PeerSession;
import com.acgist.snail.pojo.session.StatisticsSession;
import com.acgist.snail.utils.ThreadUtils;

public class PeerSessionTest {

	@Test
	public void memory() {
		PeerSession peerSession = PeerSession.newInstance(new StatisticsSession(), "192.168.1.1", 20000);
		PeerSession pexSource = PeerSession.newInstance(new StatisticsSession(), "192.168.1.1", 20000);
		peerSession.pexSource(pexSource);
		System.out.println(peerSession);
		ThreadUtils.sleep(Long.MAX_VALUE);
	}
	
}
