package com.acgist.snail.net.torrent.peer;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.acgist.snail.utils.Performance;

public class PeerServiceTest extends Performance {

	@Test
	public void testPeerId() {
		this.log(PeerService.getInstance().peerId());
		this.log(PeerService.getInstance().peerIdUrl());
		assertEquals(20, PeerService.getInstance().peerId().length);
	}
	
}
