package com.acgist.snail.net.torrent.bootstrap;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.pojo.bean.TorrentPiece;
import com.acgist.snail.pojo.session.TorrentSession;
import com.acgist.snail.protocol.torrent.bean.Torrent;
import com.acgist.snail.protocol.torrent.bean.TorrentFile;
import com.acgist.snail.protocol.torrent.bean.TorrentInfo;
import com.acgist.snail.system.config.DownloadConfig;
import com.acgist.snail.system.config.SystemConfig;
import com.acgist.snail.system.context.SystemThreadContext;
import com.acgist.snail.system.exception.NetException;
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
	/**
	 * 被选中的Piece位图
	 */
	private final BitSet selectPieces;
	private final Torrent torrent;
	private final List<TorrentStream> streams;
	private final TorrentSession torrentSession;
	/**
	 * 缓冲大小：数据下载时修改
	 */
	private final AtomicLong fileBuffer;
	/**
	 * 是否初始化完成
	 */
	private volatile boolean done = false;

	private TorrentStreamGroup(BitSet pieces, BitSet selectPieces, List<TorrentStream> streams, TorrentSession torrentSession) {
		this.pieces = pieces;
		this.selectPieces = selectPieces;
		this.streams = streams;
		this.torrent = torrentSession.torrent();
		this.torrentSession = torrentSession;
		this.fileBuffer = new AtomicLong(0);
	}
	
	public static final TorrentStreamGroup newInstance(String folder, List<TorrentFile> files, TorrentSession torrentSession) {
		final Torrent torrent = torrentSession.torrent();
		final TorrentInfo torrentInfo = torrent.getInfo();
		final boolean complete = torrentSession.completed();
		final BitSet pieces = new BitSet(torrentInfo.pieceSize());
		final BitSet selectPieces = new BitSet(torrentInfo.pieceSize());
		final List<TorrentStream> streams = new ArrayList<>(files.size());
		final TorrentStreamGroup torrentStreamGroup = new TorrentStreamGroup(pieces, selectPieces, streams, torrentSession);
		final int fileCount = (int) files.stream()
			.filter(file -> file.selected())
			.count(); // 下载文件数量
		final CountDownLatch allReady = new CountDownLatch(fileCount); // 全部完成：异步线程也执行完成
		if(CollectionUtils.isNotEmpty(files)) {
			long pos = 0;
			for (TorrentFile file : files) {
				try {
					if(file.selected()) {
						final TorrentStream stream = TorrentStream.newInstance(torrentInfo.getPieceLength(), torrentStreamGroup.fileBuffer, torrentStreamGroup);
						stream.buildFile(FileUtils.file(folder, file.path()), file.getLength(), pos, selectPieces, complete, allReady);
						streams.add(stream);
					}
				} catch (Exception e) {
					LOGGER.error("TorrentStream文件创建异常：{}", file.path(), e);
				}
				pos += file.getLength();
			}
		}
		SystemThreadContext.submit(() -> {
			try {
				allReady.await(100, TimeUnit.SECONDS);
				torrentSession.resize(torrentStreamGroup.size());
			} catch (Exception e) {
				LOGGER.error("统计下载文件大小等待异常", e);
			} finally {
				torrentStreamGroup.done = true;
			}
		});
		return torrentStreamGroup;
	}
	
	/**
	 * <p>挑选一个Piece下载</p>
	 */
	public TorrentPiece pick(final BitSet peerPieces) {
		TorrentPiece pickPiece = null;
		for (TorrentStream torrentStream : this.streams) {
			pickPiece = torrentStream.pick(peerPieces);
			if(pickPiece != null) {
				break;
			}
		}
		return pickPiece;
	}

	/**
	 * 刷出缓存
	 */
	public void flush() {
		LOGGER.debug("刷出缓存");
		for (TorrentStream torrentStream : this.streams) {
			torrentStream.flush();
		}
	}
	
	/**
	 * 下载文件大小
	 */
	public long size() {
		long size = 0L;
		for (TorrentStream torrentStream : this.streams) {
			size += torrentStream.size();
		}
		return size;
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
		return this.pieces.get(index);
	}

	/**
	 * <p>读取Piece数据</p>
	 * <p>如果跨多个文件则合并返回</p>
	 * 
	 * @param index 块序号
	 * @param begin 块偏移
	 * @param length 数据长度
	 */
	public byte[] read(final int index, final int begin, final int length) throws NetException {
		if(length >= SystemConfig.MAX_NET_BUFFER_SIZE) {
			throw new NetException("超过最大的网络包大小：" + length);
		}
		final ByteBuffer buffer = ByteBuffer.allocate(length);
		for (TorrentStream torrentStream : this.streams) {
			final byte[] bytes = torrentStream.read(index, length, begin);
			if(bytes != null) {
				buffer.put(bytes);
				if(buffer.position() >= length) {
					break;
				}
			}
		}
		if(buffer.position() < length) { // 如果数据读取不满足要求
			LOGGER.warn("读取Piece数据不满足要求，读取长度：{}，要求长度：{}", buffer.position(), length);
			return null;
		}
		return buffer.array();
	}

	/**
	 * <p>保存Piece</p>
	 * <p>调用每个{@link TorrentStream#piece}进行保存。</p>
	 * 
	 * @return 是否保存成功
	 */
	public boolean piece(TorrentPiece piece) {
		boolean ok = false;
		for (TorrentStream torrentStream : this.streams) {
			if(torrentStream.piece(piece)) {
				ok = true; // 不能跳出，可能存在一个Piece多个文件的情况。
			}
		}
		final long oldValue = this.fileBuffer.get();
		if(oldValue > DownloadConfig.getMemoryBufferByte()) {
			if(this.fileBuffer.compareAndSet(oldValue, 0)) {
				LOGGER.debug("缓冲区占满刷新缓存");
				this.flush();
			}
		}
		return ok;
	}
	
	/**
	 * <p>设置已下载的Piece，同时发出have消息。</p>
	 */
	public void piece(int index) {
		this.pieces.set(index, true);
		if(this.done) { // 初始化完成才开始发送have消息
			this.torrentSession.have(index);
		}
	}
	
	/**
	 * 已下载位图
	 */
	public BitSet pieces() {
		return this.pieces;
	}
	
	/**
	 * 需要下载位图
	 */
	public BitSet selectPieces() {
		return this.selectPieces;
	}
	
	/**
	 * 获取Piece的Hash校验
	 */
	public byte[] pieceHash(int index) {
		final byte[] pieces = this.torrent.getInfo().getPieces();
		final byte[] value = new byte[TorrentInfo.PIECE_HASH_LENGTH];
		System.arraycopy(pieces, index * TorrentInfo.PIECE_HASH_LENGTH, value, 0, TorrentInfo.PIECE_HASH_LENGTH);
		return value;
	}

	/**
	 * <p>Piece下载失败</p>
	 * <p>调用每个{@link TorrentStream#undone}进行设置。</p>
	 */
	public void undone(TorrentPiece piece) {
		for (TorrentStream torrentStream : this.streams) {
			torrentStream.undone(piece);
		}
	}

	/**
	 * 剩余没有下载Piece的数量
	 * 
	 * @since 1.0.2
	 */
	public int remainingPieceSize() {
		return this.selectPieces.cardinality() - this.pieces.cardinality();
	}
	
	/**
	 * <p>检测是否下载完成</p>
	 * <p>所有的TorrentStream完成才能判断为完成。</p>
	 */
	public boolean complete() {
		for (TorrentStream torrentStream : this.streams) {
			if(!torrentStream.complete()) {
				return false;
			}
		}
		return true;
	}

	/**
	 * 资源释放
	 */
	public void release() {
		LOGGER.debug("释放TorrentStreamGroup");
		for (TorrentStream torrentStream : this.streams) {
			torrentStream.release();
		}
	}

}
