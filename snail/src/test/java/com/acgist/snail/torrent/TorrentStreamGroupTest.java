package com.acgist.snail.torrent;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.acgist.snail.BaseTest;
import com.acgist.snail.exception.DownloadException;
import com.acgist.snail.exception.NetException;
import com.acgist.snail.net.torrent.TorrentManager;
import com.acgist.snail.net.torrent.bootstrap.TorrentStreamGroup;
import com.acgist.snail.pojo.ITaskSession.Status;
import com.acgist.snail.pojo.entity.TaskEntity;
import com.acgist.snail.pojo.session.TaskSession;
import com.acgist.snail.protocol.Protocol.Type;
import com.acgist.snail.utils.ArrayUtils;
import com.acgist.snail.utils.DigestUtils;
import com.acgist.snail.utils.StringUtils;
import com.acgist.snail.utils.ThreadUtils;

public class TorrentStreamGroupTest extends BaseTest {

	@Test
	public void testVerify() throws DownloadException, NetException {
		final var path = "e:/snail/verify.torrent";
		final var session = TorrentManager.getInstance().newTorrentSession(path);
		final var entity = new TaskEntity();
		entity.setFile("e:/tmp/verify/");
		entity.setType(Type.TORRENT);
		entity.setStatus(Status.COMPLETE);
		session.upload(TaskSession.newInstance(entity));
		final var files = session.torrent().getInfo().files();
		// 选择下载文件
		files.forEach(file -> {
			file.selected(true);
		});
		final var group = TorrentStreamGroup.newInstance("e:/tmp/verify", files, session);
		ThreadUtils.sleep(10000); // 等待任务准备完成
		final var downloadPieces = group.pieces();
		int index = downloadPieces.nextSetBit(0);
		final int length = session.torrent().getInfo().getPieceLength().intValue();
		this.log("Piece长度：{}", length);
		this.log("Piece数量：{}", session.torrent().getInfo().pieceSize());
		this.cost();
		while(!downloadPieces.isEmpty() && downloadPieces.nextSetBit(index) >= 0) {
			var piece = group.read(index, 0, length);
			if(piece == null) {
				this.log("Piece读取错误：{}->null", index);
				index++;
				continue;
			}
			if(!ArrayUtils.equals(StringUtils.sha1(piece), group.pieceHash(index))) {
				this.log("Piece校验失败：{}->{}={}", index, StringUtils.sha1Hex(piece), StringUtils.hex(group.pieceHash(index)));
			}
			index++;
		}
		this.costed();
		this.log("已经下载Piece：{}", downloadPieces);
		this.log("选择下载Piece：{}", group.selectPieces());
	}
	
	@Test
	public void testSort() {
		var source = new ArrayList<>(List.of("4", "1", "2", "3", "2", "3"));
		var target = List.of("1", "3", "2");
		source.sort((a, b) -> {
			int indexA = target.indexOf(a);
			int indexB = target.indexOf(b);
			return Integer.compare(indexA, indexB);
//			return indexA > indexB ? 1 : (indexA == indexB ? 0 : -1);
		});
		this.log(source);
		this.log(target);
	}
	
	@Test
	public void testSha1Cost() {
		final byte[] bytes = "test".getBytes();
		this.cost();
		final var digest = DigestUtils.sha1();
		for (int index = 0; index < 100000; index++) {
//			StringUtils.sha1(bytes);
			digest.digest(bytes);
			digest.reset();
		}
		this.costed();
	}

}
