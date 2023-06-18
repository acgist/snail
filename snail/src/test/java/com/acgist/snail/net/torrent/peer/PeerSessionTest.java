package com.acgist.snail.net.torrent.peer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.acgist.snail.config.PeerConfig;
import com.acgist.snail.config.PeerConfig.ExtensionType;
import com.acgist.snail.context.session.StatisticsSession;
import com.acgist.snail.utils.Performance;

class PeerSessionTest extends Performance {

	@Test
	void testPeerSession() {
		final PeerSession session = PeerSession.newInstance(new StatisticsSession(), "192.168.1.100", 18888);
		assertTrue(session.unknownClientName());
		session.piece(1);
		assertEquals(1, session.availablePieces().cardinality());
		session.piece(Integer.MIN_VALUE);
		session.piece(Integer.MAX_VALUE);
		assertEquals(1, session.availablePieces().cardinality());
		session.pieceOff(1);
		assertEquals(0, session.availablePieces().cardinality());
		session.reserved(PeerConfig.RESERVED);
		assertTrue(session.supportDhtProtocol());
		assertTrue(session.supportExtensionProtocol());
		assertTrue(session.supportFastExtensionProtocol());
		session.supportExtensionType(ExtensionType.UT_PEX, (byte) 2);
		assertEquals((byte) 2, session.extensionTypeId(ExtensionType.UT_PEX));
		session.flags((byte) 0xFF);
		assertTrue(session.utp());
		assertTrue(session.outgo());
		assertTrue(session.encrypt());
		session.status(PeerConfig.STATUS_UPLOAD);
		session.status(PeerConfig.STATUS_DOWNLOAD);
		assertTrue(session.uploading());
		assertTrue(session.downloading());
		session.statusOff(PeerConfig.STATUS_UPLOAD);
		session.statusOff(PeerConfig.STATUS_DOWNLOAD);
		assertFalse(session.uploading());
		assertFalse(session.downloading());
		assertFalse(session.supportAllowedFast());
		session.allowedPieces(1);
		assertTrue(session.supportAllowedFast());
		assertTrue(session.available());
		for (int index = 0; index < PeerConfig.MAX_FAIL_TIMES; index++) {
			session.incrementFailTimes();
		}
		assertFalse(session.available());
		session.source(PeerConfig.Source.DHT);
		session.source(PeerConfig.Source.CONNECT);
		assertEquals(2, session.sources().size());
	}
	
	@Test
	void testEquals() {
		PeerSession a = PeerSession.newInstance(null, "1234", 12);
		PeerSession b = PeerSession.newInstance(null, "1234", 12);
		assertEquals(a, b);
		a = PeerSession.newInstance(null, "1234", null);
		b = PeerSession.newInstance(null, "1234", null);
		assertEquals(a, b);
		a = PeerSession.newInstance(null, "1234", 1234);
		b = PeerSession.newInstance(null, "1234", 12);
		assertNotEquals(a, b);
		a = PeerSession.newInstance(null, "123456", 12);
		b = PeerSession.newInstance(null, "1234", 12);
		assertNotEquals(a, b);
	}
	
}
