package com.acgist.snail.net.torrent.peer;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import com.acgist.snail.context.ITaskSessionStatus.Status;
import com.acgist.snail.context.StatisticsContext;
import com.acgist.snail.context.entity.TaskEntity;
import com.acgist.snail.context.session.TaskSession;
import com.acgist.snail.net.DownloadException;
import com.acgist.snail.net.torrent.TorrentContext;
import com.acgist.snail.net.torrent.TorrentSession;
import com.acgist.snail.protocol.Protocol.Type;
import com.acgist.snail.utils.Performance;

class PeerSubMessageHandlerTest extends Performance {

    @Test
    void testHave() throws DownloadException {
        // 没有编码：GBK
        final String path = "D:/tmp/snail/ebfc2cf2ce69ba2f7aea36bbef290f0cce21386c.torrent";
        final TorrentSession session = TorrentContext.getInstance().newTorrentSession(path);
        final PeerSession peerSession = PeerSession.newInstance(StatisticsContext.getInstance().getStatistics(), "192.168.1.1", 18888);
        final PeerSubMessageHandler handler = PeerSubMessageHandler.newInstance(peerSession, session);
        final TaskEntity entity = new TaskEntity();
        entity.setFile("D:/tmp/verify/");
        entity.setType(Type.TORRENT);
        entity.setStatus(Status.AWAIT);
        session.upload(TaskSession.newInstance(entity));
        peerSession.piece(1);
        peerSession.piece(2);
        peerSession.piece(3);
        peerSession.piece(4);
        handler.have(1, 2, 3, 4);
        assertNotNull(handler);
    }
    
}
