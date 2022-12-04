package com.acgist.snail.pojo.bean;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.acgist.snail.net.torrent.TorrentPiece;
import com.acgist.snail.utils.DigestUtils;
import com.acgist.snail.utils.Performance;

class TorrentPieceTest extends Performance {

	@Test
	void testWrite() {
		final var verify = new byte[2 * 16 * 1024 - 2];
		verify[1] = 100;
		final var piece = TorrentPiece.newInstance(2 * 16 * 1024, 10, 0, 2 * 16 * 1024 - 2, DigestUtils.sha1().digest(verify), true);
		assertFalse(piece.completed());
		assertEquals(20 * 16 * 1024, piece.beginPos());
		assertEquals(22 * 16 * 1024 - 2, piece.endPos());
		assertEquals(0, piece.position());
		assertEquals(16 * 1024, piece.length());
		final var data = new byte[16 * 1024];
		data[1] = 100;
		piece.write(0, data);
		assertFalse(piece.completed());
		piece.write(piece.position(), new byte[16 * 1024 - 2]);
		assertTrue(piece.completed());
		assertTrue(piece.verify());
	}
	
	@Test
	void testRead() {
		final int pieceLength = 1024;
		final int begin = 10;
		final int end = 20;
		final var piece = TorrentPiece.newInstance(pieceLength, 0, begin, end, null, false);
		final byte[] bytes = new byte[end - begin];
		for (int index = begin; index < end; index++) {
			bytes[index - begin] = (byte) index;
		}
		piece.write(begin, bytes);
		assertArrayEquals(bytes, piece.read(0, 20));
		assertArrayEquals(new byte[] {11, 12}, piece.read(11, 2));
		assertArrayEquals(new byte[] {10, 11}, piece.read(0, 12));
		assertArrayEquals(new byte[] {18, 19}, piece.read(18, 20));
		assertNull(piece.read(0, 10));
		assertNull(piece.read(20, 10));
	}

	@Test
	void testCosted() {
		final int pieceLength = 1024;
		final int begin = 10;
		final int end = 20;
		final var piece = TorrentPiece.newInstance(pieceLength, 0, begin, end, null, false);
		final byte[] bytes = new byte[end - begin];
		for (int index = begin; index < end; index++) {
			bytes[index - begin] = (byte) index;
		}
		assertDoesNotThrow(() -> this.costed(100000, () -> piece.write(begin, bytes)));
		assertDoesNotThrow(() -> this.costed(100000, () -> piece.read(0, 20)));
	}
	
}
