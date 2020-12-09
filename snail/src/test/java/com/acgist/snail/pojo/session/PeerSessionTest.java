package com.acgist.snail.pojo.session;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.acgist.snail.BaseTest;
import com.acgist.snail.config.PeerConfig;

public class PeerSessionTest extends BaseTest {

	@Test
	public void testEquals() {
		PeerSession a = PeerSession.newInstance(null, "1234", 12);
		PeerSession b = PeerSession.newInstance(null, "1234", 12);
		this.log(a.equals(b));
		a = PeerSession.newInstance(null, "1234", null);
		b = PeerSession.newInstance(null, "1234", null);
		this.log(a.equals(b));
		a = PeerSession.newInstance(null, "1234", 1234);
		b = PeerSession.newInstance(null, "1234", 12);
		this.log(a.equals(b));
		a = PeerSession.newInstance(null, "123456", 12);
		b = PeerSession.newInstance(null, "1234", 12);
		this.log(a.equals(b));
	}
	
	@Test
	public void testSource() {
		final PeerSession peerSession = PeerSession.newInstance(null, "127.0.0.1", 8888);
		peerSession.source(PeerConfig.Source.DHT);
		peerSession.source(PeerConfig.Source.CONNECT);
		peerSession.sources().forEach(this::log);
		assertEquals(peerSession.sources().size(), 2);
	}
	
}
