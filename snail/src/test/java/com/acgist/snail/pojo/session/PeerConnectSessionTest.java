package com.acgist.snail.pojo.session;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.acgist.snail.config.DownloadConfig;
import com.acgist.snail.net.torrent.peer.PeerConnectSession;
import com.acgist.snail.utils.Performance;
import com.acgist.snail.utils.ThreadUtils;

class PeerConnectSessionTest extends Performance {

	@Test
	void testPeerConnectSession() {
		final PeerConnectSession session = new PeerConnectSession();
		assertTrue(session.isAmChoked());
		assertTrue(session.isPeerChoked());
		assertFalse(session.isAmInterested());
		assertFalse(session.isPeerInterested());
		session.amUnchoked();
		session.peerUnchoked();
		session.amInterested();
		session.peerInterested();
		assertFalse(session.isAmChoked());
		assertFalse(session.isPeerChoked());
		assertTrue(session.isAmInterested());
		assertTrue(session.isPeerInterested());
		session.upload(1024);
		session.download(1024 * 2);
		assertEquals(DownloadConfig.getUploadBufferByte(), session.uploadMark());
		assertEquals(DownloadConfig.getDownloadBufferByte(), session.downloadMark());
		ThreadUtils.sleep(60000L);
		assertEquals(1024, session.uploadMark());
		assertEquals(1024 * 2, session.downloadMark());
		ThreadUtils.sleep(30000L);
		assertEquals(1024, session.uploadMark());
		assertEquals(1024 * 2, session.downloadMark());
		ThreadUtils.sleep(30000L);
		assertEquals(0, session.uploadMark());
		assertEquals(0, session.downloadMark());
	}
	
}
