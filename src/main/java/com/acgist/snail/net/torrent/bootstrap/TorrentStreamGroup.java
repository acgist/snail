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
import com.acgist.snail.system.exception.PacketSizeException;
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
	 * 计算文件大小等待时间
	 */
	private static final int SIZE_COUNT_TIMEOUT = 100;
	
	/**
	 * 已下载Piece位图
	 */
	private final BitSet pieces;
	/**
	 * 被选中的Piece位图
	 */
	private final BitSet selectPieces;
	/**
	 * 缓冲大小：数据下载时修改
	 */
	private final AtomicLong fileBuffer;
	/**
	 * 是否初始化完成
	 */
	private volatile boolean done = false;
	
	private final Torrent torrent;
	private final List<TorrentStream> streams;
	private final TorrentSession torrentSession;

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
		// 下载文件数量
		final int fileCount = (int) files.stream()
			.filter(file -> file.selected())
			.count();
		final var startTime = System.currentTimeMillis(); // 开始时间
		// 异步线程执行完成计数器
		final CountDownLatch sizeCount = new CountDownLatch(fileCount);
		if(CollectionUtils.isNotEmpty(files)) {
			long pos = 0;
			for (TorrentFile file : files) {
				try {
					if(file.selected()) {
						final TorrentStream stream = TorrentStream.newInstance(torrentInfo.getPieceLength(), torrentStreamGroup.fileBuffer, torrentStreamGroup);
						stream.buildFile(FileUtils.file(folder, file.path()), file.getLength(), pos, selectPieces, complete, sizeCount);
						streams.add(stream);
					}
				} catch (Exception e) {
					LOGGER.error("TorrentStream创建异常：{}", file.path(), e);
				}
				pos += file.getLength();
			}
		}
		SystemThreadContext.submit(() -> {
			try {
				final var ok = sizeCount.await(SIZE_COUNT_TIMEOUT, TimeUnit.SECONDS);
				if(ok) {
					final var finishTime = System.currentTimeMillis(); // 结束时间
					LOGGER.debug("{}-任务准备完成消耗时间：{}", torrent.name(), (finishTime - startTime));
					torrentSession.resize(torrentStreamGroup.size());
				} else {
					LOGGER.warn("任务准备超时：{}", torrent.name());
				}
			} catch (InterruptedException e) {
				LOGGER.debug("统计下载文件大小等待异常", e);
				Thread.currentThread().interrupt();
			} finally {
				torrentStreamGroup.done = true;
			}
		});
		return torrentStreamGroup;
	}
	
	/**
	 * <p>发送have消息</p>
	 */
	public void have(int index) {
		if(this.done) { // 初始化完成才开始发送have消息
			this.torrentSession.have(index);
		}
	}
	
	/**
	 * <p>挑选一个Piece下载</p>
	 * 
	 * @param peerPieces Peer位图
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
	 * <p>读取Piece数据</p>
	 * <p>如果跨多个文件则合并返回</p>
	 * 
	 * @param index 块序号
	 * @param begin 块偏移
	 * @param length 数据长度
	 */
	public byte[] read(final int index, final int begin, final int length) throws NetException {
		if(length >= SystemConfig.MAX_NET_BUFFER_LENGTH || length < 0) {
			throw new PacketSizeException(length);
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
		if(buffer.position() < length) {
			LOGGER.warn("读取Piece数据错误，读取长度：{}，要求长度：{}", buffer.position(), length);
			return null;
		}
		return buffer.array();
	}

	/**
	 * <p>保存Piece数据</p>
	 * 
	 * @return 是否保存成功
	 */
	public boolean piece(TorrentPiece piece) {
		boolean ok = false;
		for (TorrentStream torrentStream : this.streams) {
			// 不能跳出，可能存在一个Piece多个文件的情况。
			if(torrentStream.piece(piece)) {
				ok = true;
			}
		}
		final long oldValue = this.fileBuffer.get();
		if(oldValue > DownloadConfig.getMemoryBufferByte()) {
			if(this.fileBuffer.compareAndSet(oldValue, 0)) {
				LOGGER.debug("缓冲区占满：刷出缓存");
				this.flush();
			}
		}
		if(ok) {
			this.have(piece.getIndex());
		}
		return ok;
	}
	
	/**
	 * 是否已下载Piece
	 * 
	 * @param index Piece序号
	 */
	public boolean havePiece(int index) {
		if(index < 0) {
			return false;
		}
		return this.pieces.get(index);
	}
	
	/**
	 * <p>设置已下载的Piece</p>
	 */
	public void done(int index) {
		synchronized (this.pieces) {
			this.pieces.set(index);
		}
	}
	
	/**
	 * <p>Piece下载失败</p>
	 */
	public void undone(TorrentPiece piece) {
		for (TorrentStream torrentStream : this.streams) {
			torrentStream.undone(piece);
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
	 * 所有位图
	 */
	public BitSet allPieces() {
		final int length = this.torrent.getInfo().pieceSize();
		final var allPieces = new BitSet(length);
		allPieces.set(0, length);
		return allPieces;
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
	 * 获取Piece的Hash校验
	 */
	public byte[] pieceHash(int index) {
		final byte[] pieces = this.torrent.getInfo().getPieces();
		final byte[] value = new byte[SystemConfig.SHA1_HASH_LENGTH];
		System.arraycopy(pieces, index * SystemConfig.SHA1_HASH_LENGTH, value, 0, SystemConfig.SHA1_HASH_LENGTH);
		return value;
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
	 * <p>检测是否下载完成</p>
	 * <p>所有的TorrentStream完成才算完成。</p>
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
