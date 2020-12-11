package com.acgist.snail.net.stun;

import org.junit.jupiter.api.Test;

import com.acgist.snail.utils.Performance;

public class StunTest extends Performance {

	@Test
	public void testMmappedAddress() {
//		StunClient client = StunClient.newInstance("stun.l.google.com", 19302);
//		StunClient client = StunClient.newInstance("stun1.l.google.com", 19302);
		StunClient client = StunClient.newInstance("stun2.l.google.com", 19302);
//		StunClient client = StunClient.newInstance("stun3.l.google.com", 19302);
//		StunClient client = StunClient.newInstance("stun4.l.google.com", 19302);
//		StunClient client = StunClient.newInstance("stun.xten.com");
//		StunClient client = StunClient.newInstance("stunserver.org");
//		StunClient client = StunClient.newInstance("numb.viagenie.ca");
//		StunClient client = StunClient.newInstance("stun.softjoys.com");
		client.mappedAddress();
		this.pause();
	}
	
}
