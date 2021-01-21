package com.acgist.snail.net.torrent;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import com.acgist.snail.context.TorrentContext;
import com.acgist.snail.context.exception.DownloadException;
import com.acgist.snail.context.exception.NetException;
import com.acgist.snail.logger.LoggerConfig;
import com.acgist.snail.pojo.ITaskSessionStatus.Status;
import com.acgist.snail.pojo.bean.TorrentFile;
import com.acgist.snail.pojo.bean.TorrentPiece;
import com.acgist.snail.pojo.entity.TaskEntity;
import com.acgist.snail.pojo.session.TaskSession;
import com.acgist.snail.pojo.wrapper.MultifileSelectorWrapper;
import com.acgist.snail.protocol.Protocol.Type;
import com.acgist.snail.utils.ArrayUtils;
import com.acgist.snail.utils.DigestUtils;
import com.acgist.snail.utils.Performance;
import com.acgist.snail.utils.StringUtils;
import com.acgist.snail.utils.ThreadUtils;

public class TorrentStreamGroupTest extends Performance {

	@Test
	public void testReload() throws DownloadException {
		final var path = "E:/snail/902FFAA29EE632C8DC966ED9AB573409BA9A518E.torrent";
		final var session = TorrentContext.getInstance().newTorrentSession(path);
		final var entity = new TaskEntity();
		entity.setFile("E:/tmp/pick/");
		entity.setType(Type.TORRENT);
		entity.setStatus(Status.COMPLETED);
		final var files = session.torrent().getInfo().files().stream()
			.filter(TorrentFile::isNotPaddingFile)
			.collect(Collectors.toList());
		final List<String> list = new ArrayList<>();
		// 加载MKV文件
		files.forEach(file -> {
			if(file.path().endsWith("mkv")) {
				file.selected(true);
				list.add(file.path());
			}
		});
		entity.setDescription(MultifileSelectorWrapper.newEncoder(list).serialize());
		session.upload(TaskSession.newInstance(entity));
		final var group = session.torrentStreamGroup();
		ThreadUtils.sleep(1000);
		this.log("------------------------");
		// 加载空文件
		group.reload(entity.getFile(), files);
		ThreadUtils.sleep(1000);
		this.log("------------------------");
		// 加载PNG文件
		files.forEach(file -> {
			if(file.path().endsWith("png")) {
				file.selected(true);
			}
		});
		group.reload(entity.getFile(), files);
		ThreadUtils.sleep(1000);
		this.log("------------------------");
		// 卸载PNG文件
		files.forEach(file -> {
			if(file.path().endsWith("png")) {
				file.selected(false);
			}
		});
		group.reload(entity.getFile(), files);
		assertNotNull(group);
	}
	
	@Test
	public void testPick() throws DownloadException {
		LoggerConfig.off();
		final var path = "E:/snail/07E1B909D8D193D80E440A8593FB57A658223A0E.torrent";
		final var session = TorrentContext.getInstance().newTorrentSession(path);
		final var entity = new TaskEntity();
		entity.setFile("E:/tmp/pick/");
		entity.setType(Type.TORRENT);
		entity.setStatus(Status.AWAIT);
		final List<String> list = new ArrayList<>();
		// 加载MKV文件
		session.torrent().getInfo().files().stream()
			.filter(TorrentFile::isNotPaddingFile)
			.forEach(file -> {
				file.selected(true);
				list.add(file.path());
			});
		entity.setDescription(MultifileSelectorWrapper.newEncoder(list).serialize());
		session.upload(TaskSession.newInstance(entity));
		final var group = session.torrentStreamGroup();
		final BitSet peerPieces = new BitSet();
		peerPieces.set(0, session.torrent().getInfo().pieceSize(), true);
		final BitSet suggestPieces = new BitSet();
//		this.costed(100000, 10, () -> {
//			group.pick(peerPieces, suggestPieces);
//		});
//		group.piecePos(620);
//		this.costed(10, 10, () -> {
//			TorrentPiece index;
//			while((index = group.pick(peerPieces, suggestPieces)) != null) {
//				this.log(index.getIndex());
//				group.done(index.getIndex());
//				this.log(session.torrent().getInfo().pieceSize());
////				group.write(index);
//			}
//		});
		this.cost();
		TorrentPiece index;
		final Set<Integer> indexSet = new HashSet<Integer>();
		while((index = group.pick(peerPieces, suggestPieces)) != null) {
			this.log(index.getIndex());
			group.done(index.getIndex());
			indexSet.add(index.getIndex());
//			group.write(index);
		}
		this.costed();
//		this.log(indexSet.size());
//		indexSet.forEach(this::log);
	}
	
	@Test
	public void testVerify() throws DownloadException, NetException {
		final var path = "e:/snail/verify.torrent";
		final var session = TorrentContext.getInstance().newTorrentSession(path);
		final var entity = new TaskEntity();
		entity.setFile("e:/tmp/verify/");
		entity.setType(Type.TORRENT);
		entity.setStatus(Status.COMPLETED);
		final List<String> list = new ArrayList<>();
		// 加载MKV文件
		session.torrent().getInfo().files().stream()
			.filter(TorrentFile::isNotPaddingFile)
			.forEach(file -> {
				if(file.path().endsWith("mkv")) {
					file.selected(true);
					list.add(file.path());
				}
			});
		entity.setDescription(MultifileSelectorWrapper.newEncoder(list).serialize());
		session.upload(TaskSession.newInstance(entity));
		final var group = session.torrentStreamGroup();
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
	public void testSHA1Costed() {
		final byte[] bytes = "test".getBytes();
		final var digest = DigestUtils.sha1();
		this.costed(100000, () -> {
			digest.digest(bytes);
//			digest.reset(); // 可以不用调用
//			StringUtils.sha1(bytes);
		});
	}

}
