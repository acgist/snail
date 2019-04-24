package com.acgist.snail.downloader.torrent.bootstrap;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.pojo.bean.TorrentPiece;
import com.acgist.snail.pojo.session.TorrentSession;
import com.acgist.snail.protocol.torrent.bean.Torrent;
import com.acgist.snail.protocol.torrent.bean.TorrentFile;
import com.acgist.snail.protocol.torrent.bean.TorrentInfo;
import com.acgist.snail.utils.CollectionUtils;
import com.acgist.snail.utils.FileUtils;

/**
 * <p>TorrentStream组</p>
 * <p>Torrent任务对下载文件进行管理。</p>
 * 
 * @author acgist
 * @since 1.0.0
 */
public class TorrentStreamGroup {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(TorrentStreamGroup.class);

	/**
	 * 已下载Piece位图
	 */
	private final BitSet pieces;
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
						final TorrentStream stream = TorrentStream.newInstance(torrentInfo.getPieceLength(), group);
						stream.buildFile(FileUtils.file(folder, file.path()), file.getLength(), pos);
						streams.add(stream);
					}
				} catch (Exception e) {
					LOGGER.error("TorrentStream文件创建异常：{}", file.path(), e);
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
	 * <p>挑选一个Piece下载</p>
	 */
	public TorrentPiece pick(final BitSet peerPieces) {
		TorrentPiece pickPiece = null;
		for (TorrentStream torrentStream : streams) {
			pickPiece = torrentStream.pick(peerPieces);
			if(pickPiece != null) {
				break;
			}
		}
		return pickPiece;
	}

	/**
	 * <p>检测是否下载完成</p>
	 * <p>所有的TorrentStream完成才能判断为完成。</p>
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
	 * <p>设置已下载的Piece，同时发出have消息。</p>
	 */
	public void piece(int index) {
		pieces.set(index, true);
		torrentSession.have(index);
	}
	
	/**
	 * 已下载位图
	 */
	public BitSet pieces() {
		return pieces;
	}
	
	/**
	 * 下载文件大小
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
	 * 是否已下载Piece
	 * 
	 * @param index Piece序号
	 */
	public boolean have(int index) {
		if(index < 0) {
			return false;
		}
		return pieces.get(index);
	}
	
	/**
	 * 下载失败
	 */
	public void undone(TorrentPiece piece) {
		for (TorrentStream torrentStream : streams) {
			torrentStream.undone(piece);
		}
	}

	/**
	 * <p>读取Piece数据</p>
	 * <p>如果跨多个文件则合并返回</p>
	 * 
	 * @param index 块序号
	 * @param begin 块偏移
	 * @param length 数据长度
	 */
	public byte[] read(final int index, final int begin, final int length) {
		final ByteBuffer buffer = ByteBuffer.allocate(length);
		for (TorrentStream torrentStream : streams) {
			final byte[] bytes = torrentStream.read(index, length, begin);
			if(bytes != null) {
				buffer.put(bytes);
				if(buffer.position() >= length) {
					break;
				}
			}
		}
		if(buffer.position() < length) { // 如果数据读取不满足要求
			return null;
		}
		return buffer.array();
	}

}
