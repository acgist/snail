package com.acgist.snail.net.torrent;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.jupiter.api.Test;

import com.acgist.snail.context.exception.DownloadException;
import com.acgist.snail.pojo.ITaskSession.Status;
import com.acgist.snail.pojo.bean.TorrentFile;
import com.acgist.snail.pojo.entity.TaskEntity;
import com.acgist.snail.pojo.session.TaskSession;
import com.acgist.snail.pojo.wrapper.MultifileSelectorWrapper;
import com.acgist.snail.protocol.Protocol.Type;
import com.acgist.snail.utils.FileUtils;
import com.acgist.snail.utils.Performance;

public class TorrentStreamTest extends Performance {

	@Test
	public void testReadWrite() throws DownloadException, IOException {
		final var path = "E:/snail/902FFAA29EE632C8DC966ED9AB573409BA9A518E.torrent";
		final var session = TorrentManager.getInstance().newTorrentSession(path);
		final var entity = new TaskEntity();
		entity.setFile("E:/snail/tmp/");
		entity.setType(Type.TORRENT);
		entity.setStatus(Status.COMPLETE);
		final var files = session.torrent().getInfo().files();
		long pos = 0L;
		TorrentFile torrentFile = null;
		for (TorrentFile file : files) {
			if(file.path().endsWith("Box_1.png")) {
				torrentFile = file;
				break;
			}
			pos += file.getLength();
		}
		final List<String> list = new ArrayList<>();
		final var wrapper = MultifileSelectorWrapper.newEncoder(list);
		entity.setDescription(wrapper.serialize());
		session.upload(TaskSession.newInstance(entity));
		final var group = session.torrentStreamGroup();
		final long pieceLength = session.torrent().getInfo().getPieceLength();
		final String sourceFile = "E:/snail/server/Scans/Vol.1/Box_1.png";
		final var oldStream = TorrentStream.newInstance(
			pieceLength,
			sourceFile,
			torrentFile.getLength(),
			pos,
			new AtomicLong(),
			group,
			false,
			new BitSet()
		);
		oldStream.verify();
		final String targetFile = "E:/snail/tmp/server/Scans/Vol.1/Box_1.png";
		final var newStream = TorrentStream.newInstance(
			pieceLength,
			targetFile,
			torrentFile.getLength(),
			pos,
			new AtomicLong(),
			group,
			false,
			new BitSet()
		);
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
