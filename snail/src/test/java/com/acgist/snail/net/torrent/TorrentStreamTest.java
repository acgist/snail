package com.acgist.snail.net.torrent;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.jupiter.api.Test;

import com.acgist.snail.context.TorrentContext;
import com.acgist.snail.context.exception.DownloadException;
import com.acgist.snail.pojo.ITaskSessionStatus.Status;
import com.acgist.snail.pojo.bean.TorrentFile;
import com.acgist.snail.pojo.entity.TaskEntity;
import com.acgist.snail.pojo.session.TaskSession;
import com.acgist.snail.pojo.wrapper.DescriptionWrapper;
import com.acgist.snail.protocol.Protocol.Type;
import com.acgist.snail.utils.FileUtils;
import com.acgist.snail.utils.Performance;

public class TorrentStreamTest extends Performance {

	@Test
	public void testReadWrite() throws DownloadException, IOException {
		final var path = "E:/snail/902FFAA29EE632C8DC966ED9AB573409BA9A518E.torrent";
		final var session = TorrentContext.getInstance().newTorrentSession(path);
		final var entity = new TaskEntity();
		entity.setFile("E:/snail/tmp/");
		entity.setType(Type.TORRENT);
		entity.setStatus(Status.COMPLETED);
		long pos = 0L;
		TorrentFile torrentFile = null;
		final var files = session.torrent().getInfo().files();
		for (TorrentFile file : files) {
			if(file.path().endsWith("Box_1.png")) {
				torrentFile = file;
				break;
			}
			pos += file.getLength();
		}
		final List<String> list = new ArrayList<>();
		entity.setDescription(DescriptionWrapper.newEncoder(list).serialize());
		session.upload(TaskSession.newInstance(entity));
		final var group = session.torrentStreamGroup();
		final long pieceLength = session.torrent().getInfo().getPieceLength();
		final String sourceFile = "E:/snail/server/Scans/Vol.1/Box_1.png";
		final var oldStream = TorrentStream.newInstance(
			pieceLength,
			sourceFile,
			torrentFile.getLength(),
			pos,
			false,
			new AtomicLong(),
			group
		);
		oldStream.install();
		oldStream.verify();
		final String targetFile = "E:/snail/tmp/server/Scans/Vol.1/Box_1.png";
		final var newStream = TorrentStream.newInstance(
			pieceLength,
			targetFile,
			torrentFile.getLength(),
			pos,
			false,
			new AtomicLong(),
			group
		);
		newStream.install();
		newStream.verify();
		int begin = (int) (pos / pieceLength);
		int end = (int) ((pos + torrentFile.getLength()) / pieceLength) + 1;
		final BitSet peerPieces = new BitSet();
		peerPieces.set(begin, end);
		this.cost();
		for (int index = begin; index < end; index++) {
			this.log(index);
			var torrentPiece = newStream.pick(0, peerPieces, peerPieces);
			final byte[] bytes = oldStream.read(index);
			if(torrentPiece != null) {
				torrentPiece.write(torrentPiece.getBegin(), bytes);
				newStream.write(torrentPiece);
			}
		}
		this.costed();
		newStream.release();
		final var sourceHash = FileUtils.sha1(sourceFile);
		final var targetHash = FileUtils.sha1(targetFile);
		assertEquals(targetHash, sourceHash);
	}
	
}
