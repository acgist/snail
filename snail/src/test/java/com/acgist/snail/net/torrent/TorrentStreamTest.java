package com.acgist.snail.net.torrent;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.acgist.snail.context.ITaskSessionStatus.Status;
import com.acgist.snail.context.entity.TaskEntity;
import com.acgist.snail.context.session.TaskSession;
import com.acgist.snail.context.wrapper.DescriptionWrapper;
import com.acgist.snail.net.DownloadException;
import com.acgist.snail.protocol.Protocol.Type;
import com.acgist.snail.utils.FileUtils;
import com.acgist.snail.utils.Performance;

class TorrentStreamTest extends Performance {

    @Test
    void testReadWrite() throws DownloadException, IOException {
        final String path = "D:/tmp/snail/902FFAA29EE632C8DC966ED9AB573409BA9A518E.torrent";
        final TorrentSession session = TorrentContext.getInstance().newTorrentSession(path);
        final TaskEntity entity = new TaskEntity();
        entity.setFile("D:/tmp/none");
        entity.setType(Type.TORRENT);
        entity.setName("acgist");
        entity.setStatus(Status.COMPLETED);
        long pos = 0L;
        TorrentFile torrentFile = null;
        final List<TorrentFile> files = session.torrent().getInfo().files();
        for (TorrentFile file : files) {
            if(file.path().endsWith("Scans/Vol.1/Box_1.png")) {
                torrentFile = file;
                break;
            }
            pos += file.getLength();
        }
        final List<String> list = new ArrayList<>();
        entity.setDescription(DescriptionWrapper.newEncoder(list).serialize());
        session.upload(TaskSession.newInstance(entity));
        final TorrentStreamGroup group = session.torrentStreamGroup();
        final long pieceLength = session.torrent().getInfo().getPieceLength();
        final String sourceFile = "D:/tmp/snail/server/Scans/Vol.1/Box_1.png";
        final TorrentStream oldStream = TorrentStream.newInstance(
            pieceLength,
            sourceFile,
            torrentFile.getLength(),
            pos,
            true,
            group
        );
        oldStream.install();
        oldStream.verify();
        final String targetFile = "D:/tmp/none/server/Scans/Vol.1/Box_1.png";
        final TorrentStream newStream = TorrentStream.newInstance(
            pieceLength,
            targetFile,
            torrentFile.getLength(),
            pos,
            false,
            group
        );
        newStream.install();
        newStream.verify();
        int pieceBeginIndex = (int) (pos / pieceLength);
        int pieceEndIndex = (int) ((pos + torrentFile.getLength()) / pieceLength) + 1;
        final BitSet peerPieces = new BitSet();
        peerPieces.set(pieceBeginIndex, pieceEndIndex);
        this.cost();
        for (int index = pieceBeginIndex; index < pieceEndIndex; index++) {
            final TorrentPiece torrentPiece = newStream.pick(0, peerPieces, peerPieces);
            if(torrentPiece != null) {
                torrentPiece.write(torrentPiece.getBegin(), oldStream.read(index));
                newStream.write(torrentPiece);
            }
        }
        this.costed();
        oldStream.release();
        newStream.release();
        final String sourceHash = FileUtils.sha1(sourceFile);
        final String targetHash = FileUtils.sha1(targetFile);
        assertEquals(targetHash, sourceHash);
    }
    
}
