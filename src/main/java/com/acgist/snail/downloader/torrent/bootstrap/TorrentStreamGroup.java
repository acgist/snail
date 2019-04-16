package com.acgist.snail.downloader.torrent.bootstrap;

import java.nio.ByteBuffer;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.pojo.TorrentPiece;
import com.acgist.snail.pojo.session.TorrentSession;
import com.acgist.snail.protocol.torrent.bean.Torrent;
import com.acgist.snail.protocol.torrent.bean.TorrentFile;
import com.acgist.snail.protocol.torrent.bean.TorrentInfo;
import com.acgist.snail.utils.CollectionUtils;

/**
 * 文件组
 */
public class TorrentStreamGroup {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(TorrentStreamGroup.class);

	/**
	 * 已下载Piece位图
	 */
	private final BitSet pieces;
	/**
	 * 文件流
	 */
	private final List<TorrentStream> streams;
	private final TorrentSession torrentSession;

	private TorrentStreamGroup(BitSet pieces, List<TorrentStream> streams, TorrentSession torrentSession) {
		this.pieces = pieces;
		this.streams = streams;
		this.torrentSession = torrentSession;
	}
	
	public static final TorrentStreamGroup newInstance(String folder, Torrent torrent, List<TorrentFile> files, TorrentSession torrentSession) {
		final TorrentInfo torrentInfo = torrent.getInfo();
		final BitSet pieces = new BitSet(torrentInfo.pieceSize());
		final List<TorrentStream> streams = new ArrayList<>(files.size());
		final TorrentStreamGroup group = new TorrentStreamGroup(pieces, streams, torrentSession);
		if(CollectionUtils.isNotEmpty(files)) {
			long pos = 0;
			for (TorrentFile file : files) {
				try {
					if(file.selected()) {
						TorrentStream stream = new TorrentStream(torrentInfo.getPieceLength(), group);
						stream.buildFile(Paths.get(folder, file.path()).toString(), file.getLength(), pos);
						streams.add(stream);
					}
				} catch (Exception e) {
					LOGGER.error("创建文件异常", e);
				}
				pos += file.getLength();
			}
			if(LOGGER.isDebugEnabled()) {
				LOGGER.debug("当前任务已下载Piece数量：{}，剩余下载Piece数量：{}",
					group.pieces.cardinality(),
					torrentInfo.pieceSize() - group.pieces.cardinality()
				);
			}
		}
		return group;
	}
	
	/**
	 * 挑选一个下载
	 */
	public TorrentPiece pick(final BitSet peerBitSet) {
		TorrentPiece pickPiece = null;
		for (TorrentStream torrentStream : streams) {
			pickPiece = torrentStream.pick(peerBitSet);
			if(pickPiece != null) {
				break;
			}
		}
		return pickPiece;
	}

	/**
	 * 检测是否下载完成
	 */
	public boolean over() {
		for (TorrentStream torrentStream : streams) {
			if(!torrentStream.over()) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * 设置已下载的Piece
	 */
	public void piece(int index) {
		pieces.set(index, true);
		torrentSession.have(index);
	}
	
	/**
	 * 获取已下载位图
	 */
	public BitSet pieces() {
		return pieces;
	}
	
	/**
	 * 获取下载文件大小
	 */
	public long size() {
		long size = 0L;
		for (TorrentStream torrentStream : streams) {
			size += torrentStream.size();
		}
		return size;
	}

	/**
	 * 保存Piece
	 */
	public void piece(TorrentPiece piece) {
		for (TorrentStream torrentStream : streams) {
			torrentStream.piece(piece);
		}
	}
	
	/**
	 * 资源释放
	 */
	public void release() {
		LOGGER.debug("释放TorrentStreamGroup");
		for (TorrentStream torrentStream : streams) {
			torrentStream.release();
		}
	}

	/**
	 * 判断是否有块数据
	 * @param index 块索引
	 */
	public boolean have(int index) {
		return pieces.get(index);
	}
	
	/**
	 * 设置下载失败
	 * @param index 块索引
	 */
	public void undone(int index) {
		for (TorrentStream torrentStream : streams) {
			torrentStream.undone(index);
		}
	}

	/**
	 * 读取块数据
	 * @param index 块索引
	 * @param begin 块偏移
	 * @param length 数据长度
	 */
	public byte[] read(final int index, final int begin, final int length) {
		final ByteBuffer buffer = ByteBuffer.allocate(length);
		for (TorrentStream torrentStream : streams) {
			byte[] bytes = torrentStream.read(index, length, begin);
			if(bytes != null) {
				buffer.put(bytes);
				if(buffer.position() >= length) {
					break;
				}
			}
		}
		if(buffer.position() < length) {
			return null;
		}
		return buffer.array();
	}

}
