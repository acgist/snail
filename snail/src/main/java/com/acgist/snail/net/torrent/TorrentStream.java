package com.acgist.snail.net.torrent;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicLong;

import com.acgist.snail.config.SystemConfig;
import com.acgist.snail.logger.Logger;
import com.acgist.snail.logger.LoggerFactory;
import com.acgist.snail.net.DownloadException;
import com.acgist.snail.utils.BeanUtils;
import com.acgist.snail.utils.DigestUtils;
import com.acgist.snail.utils.FileUtils;
import com.acgist.snail.utils.IoUtils;
import com.acgist.snail.utils.StringUtils;

/**
 * <p>文件流</p>
 * 
 * @author acgist
 */
public final class TorrentStream {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(TorrentStream.class);
	
	/**
	 * <p>文件流模式：{@value}</p>
	 */
	private static final String STREAM_MODE = "rw";

	/**
	 * <p>文件是否选择下载</p>
	 */
	private volatile boolean selected;
	/**
	 * <p>Piece大小</p>
	 */
	private final long pieceLength;
	/**
	 * <p>文件路径</p>
	 */
	private final String filePath;
	/**
	 * <p>文件大小</p>
	 */
	private final long fileSize;
	/**
	 * <p>文件开始偏移：包含该值</p>
	 */
	private final long fileBeginPos;
	/**
	 * <p>文件结束偏移：不含该值</p>
	 */
	private final long fileEndPos;
	/**
	 * <p>文件Piece开始索引</p>
	 */
	private final int fileBeginPieceIndex;
	/**
	 * <p>文件Piece结束索引</p>
	 */
	private final int fileEndPieceIndex;
	/**
	 * <p>文件Piece数量</p>
	 */
	private final int filePieceSize;
	/**
	 * <p>文件已经下载大小</p>
	 */
	private final AtomicLong fileDownloadSize;
	/**
	 * <p>已经下载Piece位图</p>
	 * <p>选择Piece需要排除</p>
	 */
	private final BitSet pieces;
	/**
	 * <p>暂停Piece位图</p>
	 * <p>上次下载失败Piece：选择Piece需要排除（选择成功清除）</p>
	 */
	private final BitSet pausePieces;
	/**
	 * <p>正在下载Piece位图</p>
	 * <p>选择Piece需要排除</p>
	 */
	private final BitSet downloadPieces;
	/**
	 * <p>Piece缓存队列</p>
	 */
	private final BlockingQueue<TorrentPiece> cachePieces;
	/**
	 * <p>文件流</p>
	 * 
	 * TODO：使用NIO（FileChannel或者MappedByteBuffer）结合TorrentPiece优化读写性能（没太大必要毕竟下载最重要的问题是网络IO）
	 */
	private final RandomAccessFile fileStream;
	/**
	 * <p>文件流组</p>
	 */
	private final TorrentStreamGroup torrentStreamGroup;
	
	/**
	 * @param pieceLength Piece大小
	 * @param path 文件路径
	 * @param size 文件大小
	 * @param pos 文件开始偏移
	 * @param completed 是否完成
	 * @param torrentStreamGroup 文件流组
	 * 
	 * @throws DownloadException 下载异常
	 */
	private TorrentStream(long pieceLength, String path, long size, long pos, boolean completed, TorrentStreamGroup torrentStreamGroup) throws DownloadException {
		this.pieceLength = pieceLength;
		this.filePath = path;
		this.fileSize = size;
		this.fileBeginPos = pos;
		this.fileEndPos = pos + size;
		this.fileBeginPieceIndex = (int) (this.fileBeginPos / this.pieceLength);
		this.fileEndPieceIndex = (int) (this.fileEndPos / this.pieceLength);
		final int filePieceSize = this.fileEndPieceIndex - this.fileBeginPieceIndex;
		final int endPieceSize = (int) (this.fileEndPos % this.pieceLength);
		if(endPieceSize > 0) {
			// 最后一块包含数据
			this.filePieceSize = filePieceSize + 1;
		} else {
			// 最后一块没有数据
			this.filePieceSize = filePieceSize;
		}
		this.fileDownloadSize = new AtomicLong(0);
		this.pieces = new BitSet();
		this.pausePieces = new BitSet();
		this.downloadPieces = new BitSet();
		this.cachePieces = new LinkedBlockingQueue<>();
		this.fileStream = this.buildFileStream();
		this.torrentStreamGroup = torrentStreamGroup;
		this.buildPieces(completed);
		this.buildFileDownloadSize();
		if(LOGGER.isDebugEnabled()) {
			final int downloadPieceSize = this.pieces.cardinality();
			LOGGER.debug("""
				新建文件流：{}
				文件大小：{}
				Piece大小：{}
				文件开始偏移：{}
				文件结束偏移：{}
				文件Piece数量：{}
				文件Piece开始索引：{}
				文件Piece结束索引：{}
				已经下载Piece数量：{}
				剩余下载Piece数量：{}""",
				this.filePath,
				this.pieceLength,
				this.fileSize,
				this.fileBeginPos,
				this.fileEndPos,
				this.filePieceSize,
				this.fileBeginPieceIndex,
				this.fileEndPieceIndex,
				downloadPieceSize,
				this.filePieceSize - downloadPieceSize
			);
		}
	}
	
	/**
	 * <p>新建文件流</p>
	 * 
	 * @param pieceLength Piece大小
	 * @param path 文件路径
	 * @param size 文件大小
	 * @param pos 文件开始偏移
	 * @param completed 是否完成
	 * @param torrentStreamGroup 文件流组
	 * 
	 * @return {@link TorrentStream}
	 * 
	 * @throws DownloadException 下载异常
	 */
	public static final TorrentStream newInstance(long pieceLength, String path, long size, long pos, boolean completed, TorrentStreamGroup torrentStreamGroup) throws DownloadException {
		return new TorrentStream(pieceLength, path, size, pos, completed, torrentStreamGroup);
	}
	
	/**
	 * <p>新建文件流</p>
	 * 
	 * @return 文件流
	 * 
	 * @throws DownloadException 下载异常
	 */
	private RandomAccessFile buildFileStream() throws DownloadException {
		FileUtils.buildParentFolder(this.filePath);
		try {
			return new RandomAccessFile(this.filePath, STREAM_MODE);
		} catch (FileNotFoundException e) {
			throw new DownloadException("新建文件流失败：" + this.filePath, e);
		}
	}
	
	/**
	 * <p>加载文件流</p>
	 */
	public void install() {
		this.selected = true;
	}
	
	/**
	 * <p>卸载文件流</p>
	 */
	public void uninstall() {
		this.selected = false;
	}
	
	/**
	 * <p>判断是否选择下载</p>
	 * 
	 * @return 是否选择下载
	 */
	public boolean selected() {
		return this.selected;
	}
	
	/**
	 * <p>判断当前下载文件路径是否匹配文件路径</p>
	 * 
	 * @param path 文件路径
	 * 
	 * @return 是否匹配
	 */
	public boolean equalsPath(String path) {
		return StringUtils.equals(path, this.filePath);
	}
	
	/**
	 * <p>加载选择下载Piece</p>
	 * 
	 * @param selectPieces 选择下载Piece
	 */
	public void buildSelectPieces(final BitSet selectPieces) {
		selectPieces.set(this.fileBeginPieceIndex, this.fileEndPieceIndex + 1);
	}
	
	/**
	 * <p>挑选下载Piece</p>
	 * <p>优先使用Peer推荐Piece位图</p>
	 * 
	 * @param piecePos 指定下载Piece索引
	 * @param peerPieces Peer已经下载Piece位图
	 * @param suggestPieces Peer推荐Piece位图
	 * 
	 * @return 下载Piece
	 */
	public TorrentPiece pick(int piecePos, final BitSet peerPieces, final BitSet suggestPieces) {
		if(piecePos > this.fileEndPieceIndex) {
			// 超过文件范围
			return null;
		}
		if(peerPieces.isEmpty() && suggestPieces.isEmpty()) {
			// Peer没有可选Piece位图
			return null;
		}
		if(this.completed()) {
			// 文件已经下载完成
			return null;
		}
		synchronized (this) {
			final BitSet pickPieces = this.pickPieces(peerPieces, suggestPieces);
			final int indexPos = Math.max(piecePos, this.fileBeginPieceIndex);
			final int index = pickPieces.nextSetBit(indexPos);
			// 没有匹配Piece或者超过文件范围
			if(index < 0 || index > this.fileEndPieceIndex) {
				LOGGER.debug("选择Piece（没有匹配）：{}-{}-{}-{}", index, piecePos, this.fileBeginPieceIndex, this.fileEndPieceIndex);
				return null;
			}
			LOGGER.debug("选择Piece（选中）：{}-{}", index, this.downloadPieces);
			this.downloadPieces.set(index);
			// 是否验证：第一块和最后一块不用校验
			boolean verify = true;
			// Piece开始内偏移
			int begin = 0;
			if(index == this.fileBeginPieceIndex) {
				verify = false;
				begin = this.firstPiecePos();
			}
			// Piece结束内偏移
			int end = (int) this.pieceLength;
			if(index == this.fileEndPieceIndex) {
				verify = false;
				end = this.lastPiecePos();
			}
			// 快速循环挑选Piece：新建Piece数据消耗性能
			final byte[] hash = this.torrentStreamGroup.pieceHash(index);
			return TorrentPiece.newInstance(this.pieceLength, index, begin, end, hash, verify);
		}
	}

	/**
	 * <p>挑选下载Piece位图</p>
	 * <p>挑选条件：没有下载完成、不是暂停中的Piece、不是下载中的Piece</p>
	 * <p>挑选完成清除暂停Piece</p>
	 * <p>如果挑选不到数据（任务接近完成）：重复挑选下载中的Piece</p>
	 * <p>如果挑选不到数据（任务正常下载）：可以挑选暂停中的Piece</p>
	 * 
	 * @param peerPieces Peer已经下载Piece位图
	 * @param suggestPieces Peer推荐Piece位图
	 * 
	 * @return 下载Piece位图
	 */
	private BitSet pickPieces(final BitSet peerPieces, final BitSet suggestPieces) {
		final BitSet pickPieces = new BitSet();
		if(!suggestPieces.isEmpty()) {
			// 优先使用Peer推荐Piece位图
			pickPieces.or(suggestPieces);
			pickPieces.andNot(this.pieces);
			pickPieces.andNot(this.pausePieces);
			pickPieces.andNot(this.downloadPieces);
		}
		if(pickPieces.isEmpty()) {
			pickPieces.or(peerPieces);
			pickPieces.andNot(this.pieces);
			pickPieces.andNot(this.pausePieces);
			pickPieces.andNot(this.downloadPieces);
		}
		if(pickPieces.isEmpty()) {
			// 没有数据判断剩余Piece数量
			final int remainingPieceSize = this.torrentStreamGroup.remainingPieceSize();
			if(remainingPieceSize == 0) {
				LOGGER.debug("选择Piece：任务已经完成");
			} else if(remainingPieceSize <= SystemConfig.getPieceRepeatSize()) {
				// 任务接近完成：重复挑选下载中的Piece
				LOGGER.debug("选择Piece：任务接近完成");
				pickPieces.or(peerPieces);
				pickPieces.andNot(this.pieces);
			} else {
				// 任务正常下载：可以挑选暂停中的Piece
				LOGGER.debug("选择Piece：任务正常下载");
				pickPieces.or(peerPieces);
				pickPieces.andNot(this.pieces);
				pickPieces.andNot(this.downloadPieces);
			}
			if(pickPieces.isEmpty()) {
				LOGGER.debug("选择Piece：没有可用Piece");
			}
		}
		this.pausePieces.clear();
		return pickPieces;
	}
	
	/**
	 * <p>保存Piece</p>
	 * <p>保存Piece必须是完成的Piece（除了开头和结尾的Piece）</p>
	 * 
	 * @param piece Piece
	 * 
	 * @return 是否保存成功
	 */
	public boolean write(TorrentPiece piece) {
		if(!piece.contain(this.fileBeginPos, this.fileEndPos)) {
			// 文件不含当前Piece
			return false;
		}
		synchronized (this) {
			final int index = piece.getIndex();
			if(this.hasPiece(index)) {
				// 最后阶段重复选择可能导致重复下载
				LOGGER.debug("Piece已经下载完成（忽略）：{}", index);
				// 此处不能返回成功：防止计算文件已经下载大小错误
				return false;
			}
			// 加入缓存队列
			if(this.cachePieces.offer(piece)) {
				LOGGER.debug("保存Piece成功：{}", index);
				this.done(index);
				this.buildFileDownloadSize();
				// 下载完成数据刷出
				if(this.completed()) {
					this.flush();
					// 可以将文件流变为读取模式
				}
				return true;
			} else {
				LOGGER.warn("保存Piece失败：{}", index);
				return false;
			}
		}
	}
	
	/**
	 * <p>读取Piece</p>
	 * <p>默认数据大小：{@link #pieceLength}</p>
	 * <p>默认数据偏移：0</p>
	 * 
	 * @param index Piece索引
	 * 
	 * @return Piece数据
	 * 
	 * @see #read(int, int)
	 */
	public byte[] read(int index) {
		return this.read(index, (int) this.pieceLength);
	}
	
	/**
	 * <p>读取Piece</p>
	 * <p>默认数据偏移：0</p>
	 * 
	 * @param index Piece索引
	 * @param length 数据大小
	 * 
	 * @return Piece数据
	 * 
	 * @see #read(int, int, int)
	 */
	public byte[] read(int index, int length) {
		return this.read(index, length, 0);
	}
	
	/**
	 * <p>读取Piece</p>
	 * 
	 * @param index Piece索引
	 * @param length 数据大小
	 * @param pos 数据偏移
	 * 
	 * @return Piece数据
	 * 
	 * @see #read(int, int, int, boolean)
	 */
	public byte[] read(int index, int length, int pos) {
		synchronized (this) {
			return this.read(index, length, pos, false);
		}
	}
	
	/**
	 * <p>读取Piece</p>
	 * <p>如果选择Piece不在文件范围内返回：null</p>
	 * <p>如果读取数据只有部分符合文件范围：返回符合部分数据</p>
	 * 
	 * @param index Piece索引
	 * @param length 数据大小
	 * @param pos 数据偏移
	 * @param ignoreHasPiece 是否忽略已经下载Piece位图
	 * 
	 * @return Piece数据
	 */
	private byte[] read(int index, int length, int pos, boolean ignoreHasPiece) {
		// 判断Piece数据是否已经下载
		if(!ignoreHasPiece && !this.hasPiece(index)) {
			return null;
		}
		// 读取缓存数据
		final TorrentPiece cachePiece = this.cachePiece(index);
		if(cachePiece != null) {
			return cachePiece.read(pos, length);
		}
		// 读取文件数据
		// 文件偏移
		long seek = 0L;
		// 开始偏移
		final long beginPos = this.pieceLength * index + pos;
		// 结束偏移
		final long endPos = beginPos + length;
		if(beginPos >= this.fileEndPos) {
			return null;
		}
		if(endPos <= this.fileBeginPos) {
			return null;
		}
		if(beginPos <= this.fileBeginPos) {
			// Piece包含文件开始
			length = (int) (length - (this.fileBeginPos - beginPos));
		} else {
			// 文件包含Piece开始
			seek = beginPos - this.fileBeginPos;
		}
		if(endPos >= this.fileEndPos) {
			// Piece包含文件结束
			length = (int) (length - (endPos - this.fileEndPos));
		}
		try {
			final byte[] bytes = new byte[length];
			// 注意线程安全
			this.fileStream.seek(seek);
			this.fileStream.read(bytes);
			return bytes;
		} catch (IOException e) {
			LOGGER.error("读取Piece异常：{}-{}-{}-{}", index, seek, length, pos, e);
		}
		return null;
	}
	
	/**
	 * <p>获取文件已经下载大小</p>
	 * 
	 * @return 文件已经下载大小
	 */
	public long downloadSize() {
		return this.fileDownloadSize.get();
	}
	
	/**
	 * <p>设置下载完成Piece</p>
	 * 
	 * @param index Piece索引
	 */
	private void done(int index) {
		this.pieces.set(index);
		this.downloadPieces.clear(index);
		this.torrentStreamGroup.done(index);
	}

	/**
	 * <p>设置下载失败Piece</p>
	 * 
	 * @param piece Piece
	 */
	public void undone(TorrentPiece piece) {
		if(!piece.contain(this.fileBeginPos, this.fileEndPos)) {
			// 文件不含当前Piece
			return;
		}
		synchronized (this) {
			this.pausePieces.set(piece.getIndex());
			this.downloadPieces.clear(piece.getIndex());
		}
	}
	
	/**
	 * <p>判断是否下载完成</p>
	 * 
	 * @return 是否下载完成
	 */
	public boolean completed() {
		synchronized (this) {
			return this.pieces.cardinality() >= this.filePieceSize;
		}
	}
	
	/**
	 * <p>释放资源</p>
	 */
	public void release() {
		this.flush();
		IoUtils.close(this.fileStream);
	}

	/**
	 * <p>校验Piece数据</p>
	 * 
	 * @param index Piece索引
	 * @param digest SHA-1算法工具
	 * 
	 * @return 是否校验成功
	 */
	private boolean verify(int index, MessageDigest digest) {
		int pos = 0;
		int length = 0;
		boolean verify = true;
		if(index == this.fileBeginPieceIndex) {
			pos = this.firstPiecePos();
			length = this.firstPieceSize();
			verify = false;
		} else if(index == this.fileEndPieceIndex) {
			pos = 0;
			length = this.lastPieceSize();
			verify = false;
		} else {
			pos = 0;
			length = (int) this.pieceLength;
			verify = true;
		}
		final byte[] bytes = this.read(index, length, pos, true);
		if(bytes == null) {
			// 没有数据
			return false;
		} else if(verify) {
			// 校验Hash
			final byte[] hash = digest.digest(bytes);
			final byte[] verifyHash = this.torrentStreamGroup.pieceHash(index);
			return Arrays.equals(hash, verifyHash);
		} else {
			// 验证是否有数据：第一块和最后一块
			return this.hasData(bytes);
		}
	}
	
	/**
	 * <p>校验文件</p>
	 * <p>使用种子数据校验文件</p>
	 * <p>重新设置文件下载信息</p>
	 * 
	 * @return 是否成功
	 * 
	 * @throws IOException IO异常
	 */
	public boolean verify() throws IOException {
		int verifyFailCount = 0;
		final boolean empty = this.fileStream.length() == 0;
		synchronized (this) {
			final MessageDigest digest = DigestUtils.sha1();
			for (int index = this.fileBeginPieceIndex; index <= this.fileEndPieceIndex; index++) {
				if(empty) {
					verifyFailCount++;
					this.verifyFail(index);
					continue;
				}
				if(this.verify(index, digest)) {
					this.done(index);
				} else {
					verifyFailCount++;
					this.verifyFail(index);
				}
			}
			this.buildFileDownloadSize();
		}
		return verifyFailCount == 0;
	}
	
	/**
	 * <p>设置验证失败Piece</p>
	 * 
	 * @param index Piece索引
	 */
	private void verifyFail(int index) {
		LOGGER.debug("Piece校验失败：{}", index);
		this.pieces.clear(index);
		this.torrentStreamGroup.undone(index);
	}
	
	/**
	 * <p>刷出缓存</p>
	 */
	public void flush() {
		synchronized (this) {
			final var list = new ArrayList<TorrentPiece>();
			this.cachePieces.drainTo(list);
			list.forEach(this::flush);
		}
	}
	
	/**
	 * <p>写出Piece</p>
	 * 
	 * @param piece Piece
	 */
	private void flush(TorrentPiece piece) {
		final int index = piece.getIndex();
		LOGGER.debug("写出Piece：{}", index);
		// 数据偏移
		int offset = 0;
		// 文件偏移
		long seek = 0L;
		// 数据长度
		int length = piece.getLength();
		// 开始偏移
		final long beginPos = piece.beginPos();
		// 结束偏移
		final long endPos = piece.endPos();
		if(beginPos <= this.fileBeginPos) {
			// Piece包含文件开始
			offset = (int) (this.fileBeginPos - beginPos);
			length = length - offset;
		} else {
			// 文件包含Piece开始
			seek = beginPos - this.fileBeginPos;
		}
		if(endPos >= this.fileEndPos) {
			// Piece包含文件结束
			length = (int) (length - (endPos - this.fileEndPos));
		}
		try {
			final byte[] bytes = piece.getData();
			// 注意线程安全
			this.fileStream.seek(seek);
			this.fileStream.write(bytes, offset, length);
		} catch (IOException e) {
			LOGGER.error("写出Piece异常：{}-{}-{}-{}", index, seek, offset, length, e);
		}
	}
	
	/**
	 * <p>读取Piece缓存数据</p>
	 * 
	 * @param index Piece索引
	 * 
	 * @return Piece数据
	 */
	private TorrentPiece cachePiece(int index) {
		TorrentPiece torrentPiece;
		final var iterator = this.cachePieces.iterator();
		while(iterator.hasNext()) {
			torrentPiece = iterator.next();
			if(torrentPiece.getIndex() == index) {
				return torrentPiece;
			}
		}
		return null;
	}
	
	/**
	 * <p>加载已经下载Piece位图</p>
	 * 
	 * @param completed 任务是否完成
	 */
	private void buildPieces(boolean completed) {
		final MessageDigest digest = DigestUtils.sha1();
		for (int index = this.fileBeginPieceIndex; index <= this.fileEndPieceIndex; index++) {
			if(completed) {
				this.done(index);
			} else if(index == this.fileBeginPieceIndex) {
				if(this.verify(index, digest)) {
					this.done(index);
				}
			} else if(index == this.fileEndPieceIndex) {
				if(this.verify(index, digest)) {
					this.done(index);
				}
			} else if(this.torrentStreamGroup.hasPiece(index)) {
				this.done(index);
			}
		}
	}
	
	/**
	 * <p>设置文件已经下载大小</p>
	 */
	private void buildFileDownloadSize() {
		// 已经下载大小
		long size = 0L;
		// 已经下载Piece数量
		int downloadPieceSize = this.pieces.cardinality();
		if(this.fileInOnePiece()) {
			// 文件只有单个Piece：第一块和最后一块一样
			if(this.hasPiece(this.fileBeginPieceIndex)) {
				size += this.firstPieceSize();
				downloadPieceSize--;
			}
		} else {
			if(this.hasPiece(this.fileBeginPieceIndex)) {
				size += this.firstPieceSize();
				downloadPieceSize--;
			}
			if(this.hasPiece(this.fileEndPieceIndex)) {
				size += this.lastPieceSize();
				downloadPieceSize--;
			}
		}
		this.fileDownloadSize.set(size + downloadPieceSize * this.pieceLength);
	}
	
	/**
	 * <p>判断文件是否处于单个Piece</p>
	 * 
	 * @return 是否处于
	 */
	private boolean fileInOnePiece() {
		return this.fileBeginPieceIndex == this.fileEndPieceIndex;
	}
	
	/**
	 * <p>获取第一块Piece开始内偏移</p>
	 * 
	 * @return 第一块Piece开始内偏移
	 */
	private int firstPiecePos() {
		return (int) (this.fileBeginPos - (this.fileBeginPieceIndex * this.pieceLength));
	}
	
	/**
	 * <p>获取第一块Piece大小</p>
	 * 
	 * @return 第一块Piece大小
	 */
	private int firstPieceSize() {
		if(this.fileInOnePiece()) {
			return this.lastPiecePos() - this.firstPiecePos();
		} else {
			return (int) (this.pieceLength - this.firstPiecePos());
		}
	}

	/**
	 * <p>获取最后一块Piece结束内偏移</p>
	 * 
	 * @return 最后一块Piece结束内偏移
	 */
	private int lastPiecePos() {
		return (int) (this.fileEndPos - (this.fileEndPieceIndex * this.pieceLength));
	}
	
	/**
	 * <p>获取最后一块Piece大小</p>
	 * 
	 * @return 最后一块Piece大小
	 */
	private int lastPieceSize() {
		if(this.fileInOnePiece()) {
			return this.lastPiecePos() - this.firstPiecePos();
		} else {
			return this.lastPiecePos();
		}
	}

	/**
	 * <p>判断是否含有数据</p>
	 * 
	 * @return 是否含有
	 */
	private boolean hasData(byte[] bytes) {
		if(bytes == null) {
			return false;
		}
		for (byte value : bytes) {
			if(value != 0) {
				return true;
			}
		}
		return false;
	}

	/**
	 * <p>判断是否已经下载Piece数据</p>
	 * 
	 * @param index Piece索引
	 * 
	 * @return 是否已经下载
	 */
	private boolean hasPiece(int index) {
		return this.pieces.get(index);
	}
	
	@Override
	public String toString() {
		return BeanUtils.toString(this, this.filePath);
	}

}
