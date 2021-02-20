package com.acgist.snail.net.torrent.peer;

import org.junit.jupiter.api.Test;

import com.acgist.snail.context.StatisticsContext;
import com.acgist.snail.context.TorrentContext;
import com.acgist.snail.context.exception.DownloadException;
import com.acgist.snail.pojo.ITaskSessionStatus.Status;
import com.acgist.snail.pojo.entity.TaskEntity;
import com.acgist.snail.pojo.session.PeerSession;
import com.acgist.snail.pojo.session.TaskSession;
import com.acgist.snail.protocol.Protocol.Type;
import com.acgist.snail.utils.Performance;

class PeerSubMessageHandlerTest extends Performance {

	@Test
	void testHave() throws DownloadException {
		final var path = "e:/snail/07E1B909D8D193D80E440A8593FB57A658223A0E.torrent"; // 没有编码：GBK
		final var session = TorrentContext.getInstance().newTorrentSession(path);
		final var peerSession = PeerSession.newInstance(StatisticsContext.getInstance().statistics(), "192.168.1.1", 18888);
		final var handler = PeerSubMessageHandler.newInstance(peerSession, session);
		final var entity = new TaskEntity();
		entity.setFile("E:/tmp/verify/");
		entity.setType(Type.TORRENT);
		entity.setStatus(Status.AWAIT);
		session.upload(TaskSession.newInstance(entity));
		peerSession.piece(1);
		peerSession.piece(2);
		peerSession.piece(3);
		peerSession.piece(4);
		handler.have(1, 2, 3, 4);
	}
	
}
