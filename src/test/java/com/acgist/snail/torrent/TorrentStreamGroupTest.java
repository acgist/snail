package com.acgist.snail.torrent;

import org.junit.Test;

import com.acgist.snail.BaseTest;
import com.acgist.snail.net.torrent.TorrentManager;
import com.acgist.snail.net.torrent.bootstrap.TorrentStreamGroup;
import com.acgist.snail.pojo.ITaskSession.Status;
import com.acgist.snail.pojo.entity.TaskEntity;
import com.acgist.snail.pojo.session.TaskSession;
import com.acgist.snail.pojo.session.TorrentSession;
import com.acgist.snail.protocol.Protocol.Type;
import com.acgist.snail.system.exception.DownloadException;
import com.acgist.snail.system.exception.NetException;
import com.acgist.snail.utils.ArrayUtils;
import com.acgist.snail.utils.StringUtils;
import com.acgist.snail.utils.ThreadUtils;

public class TorrentStreamGroupTest extends BaseTest {

	@Test
	public void testVerify() throws DownloadException, NetException {
		String path = "e:/snail/verify.torrent";
		TorrentSession session = TorrentManager.getInstance().newTorrentSession(path);
		TaskEntity entity = new TaskEntity();
		entity.setFile("e:/tmp/verify/");
		entity.setType(Type.TORRENT);
		entity.setStatus(Status.COMPLETE);
		session.upload(TaskSession.newInstance(entity));
		var files = session.torrent().getInfo().files();
		// 选择下载文件
		files.forEach(file -> {
			file.selected(true);
		});
		TorrentStreamGroup group = TorrentStreamGroup.newInstance("e:/tmp/verify", files, session);
		while(true) {
			ThreadUtils.sleep(1000);
			if(group.ready()) {
				break;
			}
		}
		var downloadPieces = group.pieces();
		int index = downloadPieces.nextSetBit(0);
		int length = session.torrent().getInfo().getPieceLength().intValue();
		this.log("Piece长度：" + length);
		this.log("Piece数量：" + session.torrent().getInfo().pieceSize());
		this.cost();
		while(downloadPieces.nextSetBit(index) >= 0) {
			var piece = group.read(index, 0, length);
			if(piece == null) {
				this.log("Piece读取错误：" + index + "->null");
				index++;
				continue;
			}
			if(!ArrayUtils.equals(StringUtils.sha1(piece), group.pieceHash(index))) {
				this.log("Piece校验失败：" + index + "->" + StringUtils.sha1Hex(piece) + "=" + StringUtils.hex(group.pieceHash(index)));
			}
			index++;
		}
		this.costed();
		this.log("已经下载Piece：" + downloadPieces);
		this.log("选择下载Piece：" + group.selectPieces());
	}

}
