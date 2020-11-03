package com.acgist.snail.pojo.session;

import org.junit.jupiter.api.Test;

import com.acgist.snail.BaseTest;

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
	
}
