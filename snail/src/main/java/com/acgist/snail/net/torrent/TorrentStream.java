package com.acgist.snail.net.torrent;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.config.SystemConfig;
import com.acgist.snail.context.exception.DownloadException;
import com.acgist.snail.pojo.bean.TorrentPiece;
import com.acgist.snail.utils.ArrayUtils;
import com.acgist.snail.utils.BeanUtils;
import com.acgist.snail.utils.DigestUtils;
import com.acgist.snail.utils.FileUtils;
import com.acgist.snail.utils.StringUtils;

/**
 * <p>Torrent下载文件流</p>
 * <p>除了文件开头和结尾的Piece，每次下载必须是一个完整的Piece。</p>
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
	 * <p>文件结束偏移：不包含该值</p>
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
	 * <p>缓冲大小</p>
	 * 
	 * @see TorrentStreamGroup#fileBufferSize
	 */
	private final AtomicLong fileBufferSize;
	/**
	 * <p>已下载大小</p>
	 */
	private final AtomicLong fileDownloadSize;
	/**
	 * <p>已下载Piece位图</p>
	 * <p>选择Piece时排除这些Piece</p>
	 */
	private final BitSet pieces;
	/**
	 * <p>暂停Piece位图</p>
	 * <p>上次下载失败的Piece，下次选择Piece时排除这些Piece，成功选择Piece后清空。</p>
	 */
	private final BitSet pausePieces;
	/**
	 * <p>下载中Piece位图</p>
	 * <p>选择Piece时排除这些Piece</p>
	 */
	private final BitSet downloadPieces;
	/**
	 * <p>Piece缓存队列</p>
	 */
	private final BlockingQueue<TorrentPiece> cachePieces;
	/**
	 * <p>文件流</p>
	 * <p>不用使用NIO（FileChannel）没有性能提升</p>
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
	 * @param complete 是否完成
	 * @param fileBufferSize 缓冲大小
	 * @param torrentStreamGroup 文件流组
	 * 
	 * @throws DownloadException 下载异常
	 */
	private TorrentStream(
		long pieceLength, String path, long size, long pos, boolean complete,
		AtomicLong fileBufferSize, TorrentStreamGroup torrentStreamGroup
	) throws DownloadException {
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
		this.fileBufferSize = fileBufferSize;
		this.fileDownloadSize = new AtomicLong(0);
		this.pieces = new BitSet();
		this.pausePieces = new BitSet();
		this.downloadPieces = new BitSet();
		this.cachePieces = new LinkedBlockingQueue<>();
		this.fileStream = this.buildFileStream();
		this.torrentStreamGroup = torrentStreamGroup;
		this.buildPieces(complete);
		this.buildFileDownloadSize();
	}
	
	/**
	 * <p>创建文件流</p>
	 * 
	 * @param pieceLength Piece大小
	 * @param path 文件路径
	 * @param size 文件大小
	 * @param pos 文件开始偏移
	 * @param complete 是否完成
	 * @param fileBufferSize 缓冲大小
	 * @param torrentStreamGroup 文件流组
	 * 
	 * @return 文件流
	 * 
	 * @throws DownloadException 下载异常
	 */
	public static final TorrentStream newInstance(
		long pieceLength, String path, long size, long pos, boolean complete,
		AtomicLong fileBufferSize, TorrentStreamGroup torrentStreamGroup
	) throws DownloadException {
		final var stream = new TorrentStream(pieceLength, path, size, pos, complete, fileBufferSize, torrentStreamGroup);
		// TODO：{}，使用多行文本
		LOGGER.debug(
			"创建文件流信息，Piece大小：{}，文件路径：{}，文件大小：{}，文件开始偏移：{}，文件结束偏移：{}，文件Piece数量：{}，文件Piece开始索引：{}，文件Piece结束索引：{}",
			stream.pieceLength,
			stream.filePath,
			stream.fileSize,
			stream.fileBeginPos,
			stream.fileEndPos,
			stream.filePieceSize,
			stream.fileBeginPieceIndex,
			stream.fileEndPieceIndex
		);
		return stream;
	}
	
	/**
	 * <p>创建文件流</p>
	 * 
	 * @return 文件流
	 * 
	 * @throws DownloadException 下载异常
	 */
	private RandomAccessFile buildFileStream() throws DownloadException {
		// 创建文件上级目录：上级目录不存在会抛出FileNotFoundException
		FileUtils.buildFolder(this.filePath, true);
		try {
			return new RandomAccessFile(this.filePath, STREAM_MODE);
		} catch (FileNotFoundException e) {
			throw new DownloadException("创建文件流失败：" + this.filePath, e);
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
	 * <p>判断文件路径是否匹配当前下载文件路径</p>
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
	 * <p>选择未下载的Piece</p>
	 * 
	 * @param piecePos 指定下载Piece索引
	 * @param peerPieces Peer已下载Piece位图
	 * @param suggestPieces Peer推荐Piece位图：优先使用
	 * 
	 * @return 未下载的Piece
	 */
	public TorrentPiece pick(int piecePos, final BitSet peerPieces, final BitSet suggestPieces) {
		if(piecePos > this.fileEndPieceIndex) {
			// 超过文件范围
			return null;
		}
		if(peerPieces.isEmpty() && suggestPieces.isEmpty()) {
			// Peer没有已下载Piece位图
			return null;
		}
		if(this.complete()) {
			// 文件已经下载完成
			return null;
		}
		synchronized (this) {
			final BitSet pickPieces = this.pickPieces(peerPieces, suggestPieces);
			final int indexPos = Math.max(piecePos, this.fileBeginPieceIndex);
			final int index = pickPieces.nextSetBit(indexPos);
			if(
				index < 0 || // 没有匹配Piece
				index > this.fileEndPieceIndex // 超过文件范围
			) {
				LOGGER.debug("选择Piece（没有匹配）：{}-{}-{}-{}", index, piecePos, this.fileBeginPieceIndex, this.fileEndPieceIndex);
				return null;
			}
			LOGGER.debug("选择Piece（选中）：{}-{}", index, this.downloadPieces);
			this.downloadPieces.set(index); // 设置下载中
			int begin = 0; // Piece开始内偏移
			boolean verify = true; // 是否验证
			// 第一块获取开始偏移
			if(index == this.fileBeginPieceIndex) {
				verify = false;
				begin = this.firstPiecePos();
			}
			int end = (int) this.pieceLength; // Piece结束内偏移
			// 最后一块获取结束偏移
			if(index == this.fileEndPieceIndex) {
				verify = false;
				end = this.lastPiecePos();
			}
			// 快速循环挑选Piece时：创建Piece数据块消耗性能
			return TorrentPiece.newInstance(this.pieceLength, index, begin, end, this.torrentStreamGroup.pieceHash(index), verify);
		}
	}

	/**
	 * <p>选择未下载的Piece位图</p>
	 * <p>选择Piece位图条件：没有下载完成、不是暂停中的Piece、不是下载中的Piece</p>
	 * <p>选择完成后清除暂停Piece</p>
	 * <p>如果挑选不到数据（任务接近完成）：重复挑选下载中的Piece</p>
	 * <p>如果挑选不到数据（任务正常下载）：可以挑选暂停中的Piece</p>
	 * 
	 * @param peerPieces Peer已下载Piece位图
	 * @param suggestPieces Peer推荐Piece位图：优先使用
	 * 
	 * @return 未下载的Piece位图
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
			// 没有数据使用Peer已下载Piece位图
			pickPieces.or(peerPieces);
			pickPieces.andNot(this.pieces);
			pickPieces.andNot(this.pausePieces);
			pickPieces.andNot(this.downloadPieces);
		}
		if(pickPieces.isEmpty()) {
			// 没有数据判断剩余Piece数量
			final int remainingPieceSize = this.torrentStreamGroup.remainingPieceSize();
			if(remainingPieceSize == 0) {
				// 任务已经完成
				LOGGER.debug("选择Piece：没有可选Piece");
			} else if(remainingPieceSize <= SystemConfig.getPieceRepeatSize()) {
				// 任务接近完成：重复挑选下载中的Piece
				LOGGER.debug("选择Piece：任务接近完成重复选择下载中的Piece");
				pickPieces.or(peerPieces);
				pickPieces.andNot(this.pieces);
			} else {
				// 任务正常：排除暂停Piece位图
				LOGGER.debug("选择Piece：排除暂停Piece");
				pickPieces.or(peerPieces);
				pickPieces.andNot(this.pieces);
				pickPieces.andNot(this.downloadPieces);
			}
			if(pickPieces.isEmpty()) {
				LOGGER.debug("选择Piece：没有可用Piece");
			}
		}
		this.pausePieces.clear(); // 清空暂停Piece位图
		return pickPieces;
	}
	
	/**
	 * <p>保存Piece</p>
	 * <p>每次保存的必须是一个完成的Piece</p>
	 * 
	 * @param piece Piece
	 * 
	 * @return 是否保存成功
	 */
	public boolean write(TorrentPiece piece) {
		// 文件不包含当前Piece
		if(!piece.contain(this.fileBeginPos, this.fileEndPos)) {
			return false;
		}
		synchronized (this) {
			final int index = piece.getIndex();
			if(this.hasPiece(index)) {
				// 最后阶段重复选择可能导致重复下载
				LOGGER.debug("Piece已经下载完成（忽略）：{}", index);
				return false;
			}
			// 加入缓存队列
			if(this.cachePieces.offer(piece)) {
				LOGGER.debug("保存Piece：{}", index);
				this.done(index);
				// 更新缓存大小
				this.fileBufferSize.addAndGet(piece.getLength());
				// 设置已下载大小
				this.buildFileDownloadSize();
				// 下载完成数据刷出
				if(this.complete()) {
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
	 * <p>读取Piece数据</p>
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
	 * <p>读取Piece数据</p>
	 * <p>默认数据偏移：0</p>
	 * 
	 * @param index Piece索引
	 * @param size 数据大小
	 * 
	 * @return Piece数据
	 * 
	 * @see #read(int, int, int)
	 */
	public byte[] read(int index, int size) {
		return this.read(index, size, 0);
	}
	
	/**
	 * <p>读取Piece数据</p>
	 * 
	 * @param index Piece索引
	 * @param size 数据大小
	 * @param pos 数据偏移
	 * 
	 * @return Piece数据
	 * 
	 * @see #read(int, int, int, boolean)
	 */
	public byte[] read(int index, int size, int pos) {
		synchronized (this) {
			return this.read(index, size, pos, false);
		}
	}
	
	/**
	 * <p>读取Piece数据</p>
	 * <p>如果选择的Piece不在文件范围内返回：null</p>
	 * <p>如果读取数据只有部分符合文件的范围，只读取符合部分数据返回。</p>
	 * 
	 * @param index Piece索引
	 * @param size 数据大小
	 * @param pos 数据偏移
	 * @param ignoreHasPiece 是否忽略已下载Piece位图
	 * 
	 * @return Piece数据
	 */
	private byte[] read(int index, int size, int pos, boolean ignoreHasPiece) {
		// 判断Piece数据是否已经下载
		if(!ignoreHasPiece && !this.hasPiece(index)) {
			return null;
		}
		// 从Piece缓存中读取数据
		final TorrentPiece cachePiece = this.cachePiece(index);
		if(cachePiece != null) {
			return cachePiece.read(pos, size);
		}
		// 从文件中读取数据
		long seek = 0L; // 文件偏移
		final long beginPos = this.pieceLength * index + pos; // 开始偏移
		final long endPos = beginPos + size; // 结束偏移
		if(beginPos >= this.fileEndPos) {
			return null;
		}
		if(endPos <= this.fileBeginPos) {
			return null;
		}
		if(beginPos <= this.fileBeginPos) {
			// Piece包含文件开始
			size = (int) (size - (this.fileBeginPos - beginPos));
		} else {
			// 文件包含Piece开始
			seek = beginPos - this.fileBeginPos;
		}
		// Piece包含文件结束
		if(endPos >= this.fileEndPos) {
			size = (int) (size - (endPos - this.fileEndPos));
		}
		if(size <= 0) {
			return null;
		}
		try {
			final byte[] bytes = new byte[size];
			this.fileStream.seek(seek); // 注意线程安全
			this.fileStream.read(bytes);
			return bytes;
		} catch (IOException e) {
			LOGGER.error("Piece读取异常：{}-{}-{}-{}", index, seek, size, pos, e);
		}
		return null;
	}
	
	/**
	 * <p>获取文件已下载大小</p>
	 * 
	 * @return 文件已下载大小
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
		this.pieces.set(index); // 下载成功
		this.downloadPieces.clear(index); // 清除下载中的位图
		this.torrentStreamGroup.done(index); // 设置Piece下载完成
	}

	/**
	 * <p>设置下载失败Piece</p>
	 * 
	 * @param index Piece索引
	 */
	private void undone(int index) {
		LOGGER.debug("文件校验失败：{}", index);
		this.pieces.clear(index); // 清除下载成功
		this.torrentStreamGroup.undone(index); // 设置Piece下载失败
	}
	
	/**
	 * <p>设置下载失败Piece</p>
	 * 
	 * @param piece 下载失败Piece
	 */
	public void undone(TorrentPiece piece) {
		// 文件不包含当前Piece
		if(!piece.contain(this.fileBeginPos, this.fileEndPos)) {
			return;
		}
		synchronized (this) {
			this.pausePieces.set(piece.getIndex()); // 设置暂停Piece位图
			this.downloadPieces.clear(piece.getIndex()); // 清除下载中Piece位图
		}
	}
	
	/**
	 * <p>判断是否下载完成</p>
	 * 
	 * @return 是否下载完成
	 */
	public boolean complete() {
		synchronized (this) {
			return this.pieces.cardinality() >= this.filePieceSize;
		}
	}
	
	/**
	 * <p>释放资源</p>
	 * <p>将Piece缓存写入文件、关闭文件流</p>
	 */
	public void release() {
		this.flush();
		try {
			this.fileStream.close();
		} catch (IOException e) {
			LOGGER.error("TorrentStream关闭异常", e);
		}
	}

	/**
	 * <p>校验Piece数据</p>
	 * <p>第一块和最后一块Piece数据不验证Hash：直接验证是有含有数据</p>
	 * 
	 * @param index Piece索引
	 * @param digest SHA-1算法工具
	 * 
	 * @return 是否校验成功
	 */
	private boolean verify(int index, MessageDigest digest) {
		int pos = 0;
		int length = 0;
		boolean verify = true; // 是否校验
		if(this.fileInOnePiece()) {
			pos = this.firstPiecePos();
			length = this.firstPieceSize();
			verify = false;
		} else if(index == this.fileBeginPieceIndex) {
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
			return ArrayUtils.equals(hash, verifyHash);
		} else {
			// 不校验Hash：验证是否有数据
			return this.hasData(bytes);
		}
	}
	
	/**
	 * <p>校验文件</p>
	 * <p>使用种子Hash校验文件</p>
	 * <p>校验文件重新设置已下载文件信息</p>
	 * 
	 * @return 校验结果
	 * 
	 * @throws IOException IO异常
	 */
	public boolean verify() throws IOException {
		int verifyFailCount = 0; // 失败计数
		final boolean empty = this.fileStream.length() == 0; // 文件没有数据
		synchronized (this) {
			final MessageDigest digest = DigestUtils.sha1();
			for (int index = this.fileBeginPieceIndex; index <= this.fileEndPieceIndex; index++) {
				if(empty) {
					verifyFailCount++;
					this.undone(index);
					continue;
				}
				if(this.verify(index, digest)) {
					this.done(index);
				} else {
					verifyFailCount++;
					this.undone(index);
				}
			}
			this.buildFileDownloadSize();
		}
		if(LOGGER.isDebugEnabled()) {
			LOGGER.debug("当前文件流已下载Piece数量：{}，剩余下载Piece数量：{}",
				this.pieces.cardinality(),
				this.filePieceSize - this.pieces.cardinality()
			);
		}
		return verifyFailCount == 0;
	}
	
	/**
	 * <p>将Piece缓存写入文件</p>
	 */
	public void flush() {
		synchronized (this) {
			final var list = new ArrayList<TorrentPiece>();
			this.cachePieces.drainTo(list);
			list.forEach(this::flush);
		}
	}
	
	/**
	 * <p>将Piece数据写入文件</p>
	 * 
	 * @param piece Piece数据
	 */
	private void flush(TorrentPiece piece) {
		final int index = piece.getIndex();
		LOGGER.debug("Piece写入文件：{}", index);
		int offset = 0; // 数据偏移
		long seek = 0L; // 文件偏移
		int length = piece.getLength(); // Piece数据长度：计算写入长度
		final long beginPos = piece.beginPos(); // 开始偏移
		final long endPos = piece.endPos(); // 结束偏移
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
		if(length <= 0) {
			return;
		}
		try {
			final byte[] bytes = piece.getData();
			this.fileStream.seek(seek); // 注意线程安全
			this.fileStream.write(bytes, offset, length);
		} catch (IOException e) {
			LOGGER.error("Piece写入文件异常：{}-{}-{}-{}", index, seek, offset, length, e);
		}
	}
	
	/**
	 * <p>读取缓存中的Piece数据</p>
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
	 * <p>加载已下载Piece位图</p>
	 * 
	 * @param complete 任务是否完成
	 */
	private void buildPieces(boolean complete) {
		final MessageDigest digest = DigestUtils.sha1();
		for (int index = this.fileBeginPieceIndex; index <= this.fileEndPieceIndex; index++) {
			if(complete) {
				// 任务完成
				this.done(index);
			} else if(index == this.fileBeginPieceIndex) {
				// 第一块文件
				if(this.verify(index, digest)) {
					this.done(index);
				}
			} else if(index == this.fileEndPieceIndex) {
				// 最后一块文件
				if(this.verify(index, digest)) {
					this.done(index);
				}
			} else if(this.torrentStreamGroup.hasPiece(index)) {
				// 已经下载
				this.done(index);
			}
		}
		if(LOGGER.isDebugEnabled()) {
			LOGGER.debug("当前文件流已下载Piece数量：{}，剩余下载Piece数量：{}",
				this.pieces.cardinality(),
				this.filePieceSize - this.pieces.cardinality()
			);
		}
	}
	
	/**
	 * <p>设置已下载大小</p>
	 */
	private void buildFileDownloadSize() {
		long size = 0L; // 已下载大小
		// 已下载Piece数量
		int downloadPieceSize = this.pieces.cardinality();
		if(this.fileInOnePiece()) {
			// 第一块和最后一块大小一样
			if(this.hasPiece(this.fileBeginPieceIndex)) {
				size += this.firstPieceSize();
				downloadPieceSize--;
			}
		} else {
			if(this.hasPiece(this.fileBeginPieceIndex)) {
				// 计算第一块Piece大小
				size += this.firstPieceSize();
				downloadPieceSize--;
			}
			if(this.hasPiece(this.fileEndPieceIndex)) {
				// 计算最后一块Piece大小
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
	 * <p>判断是否已下载Piece数据</p>
	 * 
	 * @param index Piece索引
	 * 
	 * @return 是否已下载
	 */
	private boolean hasPiece(int index) {
		return this.pieces.get(index);
	}
	
	@Override
	public String toString() {
		return BeanUtils.toString(this, this.filePath);
	}

}
