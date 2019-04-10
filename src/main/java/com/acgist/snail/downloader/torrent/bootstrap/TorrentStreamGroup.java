package com.acgist.snail.downloader.torrent.bootstrap;

import java.nio.ByteBuffer;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.pojo.TorrentPiece;
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
	 * 已下载的块位图
	 */
	private BitSet pieces;
	/**
	 * 文件流
	 */
	private List<TorrentStream> streams;

	public static final TorrentStreamGroup newInstance(String folder, Torrent torrent, List<TorrentFile> files) {
		TorrentStreamGroup group = new TorrentStreamGroup();
		if(CollectionUtils.isNotEmpty(files)) {
			long pos = 0;
			final List<TorrentStream> streams = new ArrayList<>(files.size());
			final TorrentInfo info = torrent.getInfo();
			group.pieces = new BitSet(info.pieceSize());
			for (TorrentFile file : files) {
				try {
					if(file.selected()) {
						TorrentStream stream = new TorrentStream(info.getPieceLength(), group);
						stream.buildFile(Paths.get(folder, file.path()).toString(), file.getLength(), pos);
						streams.add(stream);
					}
				} catch (Exception e) {
					LOGGER.error("创建文件异常", e);
				}
				pos += file.getLength();
			}
			group.streams = streams;
		}
		return group;
	}
	
	private TorrentStreamGroup() {
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
	 * 设置已下载的Piece
	 */
	public void piece(int index) {
		pieces.set(index, true);
		// TODO：have消息
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
			torrentStream.pieces(piece);
		}
	}
	
	/**
	 * 资源释放
	 */
	public void release() {
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
