package com.acgist.snail.torrent;

import org.junit.Test;

import com.acgist.snail.BaseTest;
import com.acgist.snail.pojo.bean.TorrentPiece;

public class TorrentPieceTest extends BaseTest {

	@Test
	public void testRead() {
		final int pieceLength = 1024;
		final int begin = 10;
		final int end = 20;
		TorrentPiece piece = TorrentPiece.newInstance(null, pieceLength, 0, begin, end, false);
		byte[] bytes = new byte[end - begin];
		for (int index = begin; index <  end; index++) {
			bytes[index - begin] = (byte) index;
		}
		piece.write(begin, bytes);
		this.log(piece.read(0, 20)); // 完全包含
		this.log(piece.read(11, 2)); // 包含部分
		this.log(piece.read(0, 12)); // 包含开始
		this.log(piece.read(18, 20)); // 包含结束
		this.log(piece.read(0, 10)); // 不包含
		this.log(piece.read(20, 0)); // 不包含
	}
	

	@Test
	public void testCost() {
		final int pieceLength = 1024;
		final int begin = 10;
		final int end = 20;
		TorrentPiece piece = TorrentPiece.newInstance(null, pieceLength, 0, begin, end, false);
		byte[] bytes = new byte[end - begin];
		for (int index = begin; index <  end; index++) {
			bytes[index - begin] = (byte) index;
		}
		piece.write(begin, bytes);
		this.cost();
		for (int i = 0; i < 100000; i++) {
			piece.read(0, 20);
		}
		this.costed();
	}
	
}
