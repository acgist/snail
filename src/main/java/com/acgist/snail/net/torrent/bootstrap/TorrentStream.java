package com.acgist.snail.net.torrent.bootstrap;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.pojo.bean.TorrentPiece;
import com.acgist.snail.system.config.SystemConfig;
import com.acgist.snail.system.context.SystemThreadContext;
import com.acgist.snail.system.exception.DownloadException;
import com.acgist.snail.utils.ArrayUtils;
import com.acgist.snail.utils.CollectionUtils;
import com.acgist.snail.utils.FileUtils;
import com.acgist.snail.utils.ObjectUtils;
import com.acgist.snail.utils.StringUtils;

/**
 * <p>Torrent下载文件流</p>
 * <p>除了文件开头和结尾的Piece，每次下载必须是一个完整的Piece。</p>
 * 
 * @author acgist
 * @since 1.0.0
 */
public final class TorrentStream {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(TorrentStream.class);

	/**
	 * <p>文件是否被选中下载</p>
	 */
	private volatile boolean selected;
	/**
	 * <p>Piece大小</p>
	 */
	private final long pieceLength;
	/**
	 * <p>缓冲大小</p>
	 * <p>所有文件Piece缓冲数据大小（TorrentStreamGroup中的引用）</p>
	 */
	private final AtomicLong fileBuffer;
	/**
	 * <p>已下载大小</p>
	 */
	private final AtomicLong fileDownloadSize;
	/**
	 * <p>下载文件组</p>
	 */
	private final TorrentStreamGroup torrentStreamGroup;
	/**
	 * <p>Piece缓存队列</p>
	 */
	private final BlockingQueue<TorrentPiece> filePieces;
	/**
	 * <p>文件路径</p>
	 */
	private final String file;
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
	 * <p>文件Piece数量</p>
	 */
	private int filePieceSize;
	/**
	 * <p>文件Piece开始索引</p>
	 */
	private int fileBeginPieceIndex;
	/**
	 * <p>文件Piece结束索引</p>
	 */
	private int fileEndPieceIndex;
	/**
	 * <p>已下载Piece位图</p>
	 */
	private final BitSet pieces;
	/**
	 * <p>暂停Piece位图</p>
	 * <p>上次下载失败的Piece，下次请求时不选择，选择成功后清除，以后还可以选择该Piece。</p>
	 * <p>主要用来处理两个文件处于同一个Piece，并且两个文件没有同时被选择下载。</p>
	 */
	private final BitSet pausePieces;
	/**
	 * <p>下载中Piece位图</p>
	 */
	private final BitSet downloadPieces;
	/**
	 * <p>文件流</p>
	 * 
	 * TODO：替换FileChannel
	 */
	private final RandomAccessFile fileStream;
	
	private TorrentStream(
		long pieceLength, String file, long size, long pos,
		AtomicLong fileBuffer, TorrentStreamGroup torrentStreamGroup
	) throws DownloadException {
		this.pieceLength = pieceLength;
		this.file = file;
		this.fileSize = size;
		this.fileBeginPos = pos;
		this.fileEndPos = pos + size;
		this.fileBuffer = fileBuffer;
		this.torrentStreamGroup = torrentStreamGroup;
		this.pieces = new BitSet();
		this.pausePieces = new BitSet();
		this.downloadPieces = new BitSet();
		this.fileDownloadSize = new AtomicLong(0);
		this.filePieces = new LinkedBlockingQueue<>();
		this.fileStream = this.buildFileStream(); // 创建文件流
	}
	
	/**
	 * <p>创建文件流</p>
	 * 
	 * @param pieceLength Piece长度
	 * @param file 文件路径
	 * @param size 文件大小
	 * @param pos 文件开始偏移
	 * @param fileBuffer 缓冲大小
	 * @param torrentStreamGroup 文件流组
	 * @param selectPieces 被选中的Piece
	 * @param complete 是否完成
	 * @param sizeCount 异步文件加载计数器
	 * 
	 * @return 文件流
	 * 
	 * @throws DownloadException 下载异常
	 */
	public static final TorrentStream newInstance(
		long pieceLength, String file, long size, long pos,
		AtomicLong fileBuffer, TorrentStreamGroup torrentStreamGroup,
		BitSet selectPieces, boolean complete, CountDownLatch sizeCount
	) throws DownloadException {
		final var stream = new TorrentStream(pieceLength, file, size, pos, fileBuffer, torrentStreamGroup);
		stream.buildFilePiece(); // 加载文件Piece
		stream.buildFileAsyn(complete, sizeCount); // 异步加载文件
		stream.buildSelectPieces(selectPieces); // 加载被选中的Piece
		stream.install();
		// TODO：{}，使用多行文本
		LOGGER.debug(
			"创建文件流信息，Piece大小：{}，文件路径：{}，文件大小：{}，文件开始偏移：{}，文件Piece数量：{}，文件Piece开始索引：{}，文件Piece结束索引：{}",
			stream.pieceLength,
			stream.file,
			stream.fileSize,
			stream.fileBeginPos,
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
		// 创建文件父目录：父目录不存在会抛出FileNotFoundException
		FileUtils.buildFolder(this.file, true);
		try {
			return new RandomAccessFile(this.file, "rw");
		} catch (FileNotFoundException e) {
			throw new DownloadException("创建文件流失败：" + this.file, e);
		}
	}
	
	/**
	 * <p>加载文件流</p>
	 * <p>设置选中下载</p>
	 */
	public void install() {
		this.selected = true;
	}
	
	/**
	 * <p>卸载文件流</p>
	 * <p>设置不选中下载</p>
	 */
	public void uninstall() {
		this.selected = false;
	}
	
	/**
	 * <p>判断是否被选中下载</p>
	 * 
	 * @return {@code true}-选中；{@code false}-没有选中；
	 */
	public boolean selected() {
		return this.selected;
	}
	
	/**
	 * <p>判断{@code path}是不是当前文件流的文件路径</p>
	 * 
	 * @param path 文件路径
	 * 
	 * @return {@code true}-是；{@code false}-不是；
	 */
	public boolean equalsPath(String path) {
		return StringUtils.equals(path, this.file);
	}
	
	/**
	 * <p>加载被选中的Piece</p>
	 * 
	 * @param selectPieces 被选中的Piece
	 */
	public void buildSelectPieces(final BitSet selectPieces) {
		selectPieces.set(this.fileBeginPieceIndex, this.fileEndPieceIndex + 1);
	}
	
	/**
	 * <p>选择未下载的Piece</p>
	 * <p>选择Piece没有下载完成、不处于暂停Piece和下载中的Piece，选择后清除暂停的Piece。</p>
	 * <p>如果挑选不到符合条件的Piece并且任务处于接近完成状态时，那么可以选择下载中的Piece进行下载。</p>
	 * 
	 * @param peerPieces Peer已下载Piece位图
	 * @param suggestPieces Peer推荐Piece位图：优先使用
	 * 
	 * @return 下载Piece
	 */
	public TorrentPiece pick(final BitSet peerPieces, final BitSet suggestPieces) {
		if(peerPieces.isEmpty()) { // Peer没有已下载Piece数据
			return null;
		}
		synchronized (this) {
			final BitSet pickPieces = new BitSet(); // 挑选的Piece
			if(!suggestPieces.isEmpty()) {
				// 优先使用Peer推荐Piece位图
				pickPieces.or(suggestPieces);
				pickPieces.andNot(this.pieces);
				pickPieces.andNot(this.pausePieces);
				pickPieces.andNot(this.downloadPieces);
			}
			if(pickPieces.isEmpty()) {
				// Peer已下载Piece位图
				pickPieces.or(peerPieces);
				pickPieces.andNot(this.pieces);
				pickPieces.andNot(this.pausePieces);
				pickPieces.andNot(this.downloadPieces);
			}
			this.pausePieces.clear(); // 清空暂停Piece位图
			// 如果挑选不到Piece
			if(pickPieces.isEmpty()) {
				// 任务接近完成
				if(this.torrentStreamGroup.remainingPieceSize() <= SystemConfig.getPieceRepeatSize()) {
					LOGGER.debug("选择Piece：任务接近完成重复选择下载中的Piece");
					pickPieces.or(peerPieces);
					pickPieces.andNot(this.pieces);
					if(pickPieces.isEmpty()) {
						LOGGER.debug("选择Piece：Piece已经全部下载完成（接近完成）");
						return null;
					}
				} else {
					// 排除暂停Piece位图
					LOGGER.debug("选择Piece：排除暂停Piece");
					pickPieces.or(peerPieces);
					pickPieces.andNot(this.pieces);
					pickPieces.andNot(this.downloadPieces);
					if(pickPieces.isEmpty()) {
						LOGGER.debug("选择Piece：Piece已经全部下载完成（排除暂停）");
						return null;
					}
				}
			}
			final int index = pickPieces.nextSetBit(this.fileBeginPieceIndex);
			if(index == -1 || index > this.fileEndPieceIndex) {
				LOGGER.debug("选择Piece：找不到Piece");
				return null;
			}
			LOGGER.debug("下载中Piece：{}-{}", index, this.downloadPieces);
			this.downloadPieces.set(index); // 设置下载中
			int begin = 0; // Piece开始内偏移
			boolean verify = true; // 是否验证
			// 第一块获取开始偏移
			if(index == this.fileBeginPieceIndex) {
				verify = false;
				begin = firstPiecePos();
			}
			int end = (int) this.pieceLength; // Piece结束内偏移
			// 最后一块获取结束偏移
			if(index == this.fileEndPieceIndex) {
				verify = false;
				end = lastPieceSize();
			}
			return TorrentPiece.newInstance(this.torrentStreamGroup.pieceHash(index), this.pieceLength, index, begin, end, verify);
		}
	}

	/**
	 * <p>保存Piece</p>
	 * <p>每次保存的必须是一个完成的Piece</p>
	 * <p>如果不在该文件范围内则不保存</p>
	 * 
	 * @param piece Piece
	 * 
	 * @return 是否保存成功
	 */
	public boolean write(TorrentPiece piece) {
		// 不符合当前文件位置
		if(!piece.contain(this.fileBeginPos, this.fileEndPos)) {
			return false;
		}
		synchronized (this) {
			boolean ok = false;
			if(this.havePiece(piece.getIndex())) { // 最后阶段重复选中可能重复
				LOGGER.debug("Piece已经下载完成（忽略）：{}", piece.getIndex());
				return false;
			}
			if(this.filePieces.offer(piece)) { // 加入缓存队列
				ok = true;
				LOGGER.debug("保存Piece：{}", piece.getIndex());
				this.done(piece.getIndex());
				// 更新缓存大小
				this.fileBuffer.addAndGet(piece.getLength());
				// 设置已下载大小
				this.buildFileDownloadSize();
				// 下载完成数据刷出
				if(this.complete()) {
					this.flush();
				}
			} else {
				LOGGER.warn("保存Piece失败：{}", piece.getIndex());
			}
			return ok;
		}
	}
	
	/**
	 * <p>读取Piece数据</p>
	 * <p>数据大小：{@link #pieceLength}</p>
	 * <p>默认偏移：{@code 0}</p>
	 * 
	 * @param index Piece索引
	 * 
	 * @return Piece数据
	 * 
	 * @see {@link #read(int, int, int, boolean)}
	 */
	public byte[] read(int index) {
		return read(index, (int) this.pieceLength);
	}
	
	/**
	 * <p>读取Piece数据</p>
	 * <p>默认偏移：{@code 0}</p>
	 * 
	 * @param index Piece索引
	 * @param size 数据大小
	 * 
	 * @return Piece数据
	 * 
	 * @see {@link #read(int, int, int, boolean)}
	 */
	public byte[] read(int index, int size) {
		return read(index, size, 0);
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
	 * @see {@link #read(int, int, int, boolean)}
	 */
	public byte[] read(int index, int size, int pos) {
		synchronized (this) {
			return read(index, size, pos, false);
		}
	}
	
	/**
	 * <p>读取Piece数据</p>
	 * <p>如果选择的Piece不在文件范围内返回：{@code null}</p>
	 * <p>如果读取数据只有部分符合文件的范围，会自动修正范围，读取符合部分数据返回。</p>
	 * 
	 * @param index Piece索引
	 * @param size 数据大小
	 * @param pos 数据偏移
	 * @param ignorePieces 是否忽略已下载Piece位图（文件校验忽略）
	 * 
	 * @return Piece数据
	 */
	private byte[] read(int index, int size, int pos, boolean ignorePieces) {
		// 判断Piece不在文件范围内
		if(!this.haveIndex(index)) {
			return null;
		}
		// 判断Piece数据是否已经下载
		if(!ignorePieces && !this.havePiece(index)) {
			return null;
		}
		// 从Piece缓存中读取数据
		final TorrentPiece torrentPiece = this.torrentPiece(index);
		if(torrentPiece != null) {
			return torrentPiece.read(pos, size);
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
		if(beginPos <= this.fileBeginPos) { // Piece包含文件开始
			size = (int) (size - (this.fileBeginPos - beginPos));
		} else { // 文件包含Piece开始
			seek = beginPos - this.fileBeginPos;
		}
		// Piece包含文件结束
		if(endPos >= this.fileEndPos) {
			size = (int) (size - (endPos - this.fileEndPos));
		}
		if(size <= 0) {
			return null;
		}
		final byte[] bytes = new byte[size];
		try {
			this.fileStream.seek(seek); // 注意线程安全
			this.fileStream.read(bytes);
		} catch (IOException e) {
			LOGGER.error("Piece读取异常：{}-{}-{}-{}", index, size, pos, ignorePieces, e);
		}
		return bytes;
	}
	
	/**
	 * <p>获取文件已下载大小</p>
	 * 
	 * @return 文件已下载大小
	 */
	public long size() {
		return this.fileDownloadSize.get();
	}
	
	/**
	 * <p>设置下载完成Piece</p>
	 * 
	 * @param index Piece索引
	 */
	private void done(int index) {
		this.pieces.set(index); // 下载成功
		this.downloadPieces.clear(index); // 去掉下载状态
		this.torrentStreamGroup.done(index); // 设置Piece下载完成
	}

	/**
	 * <p>设置下载失败Piece</p>
	 * 
	 * @param piece 下载失败Piece
	 */
	public void undone(TorrentPiece piece) {
		// 不符合当前文件位置
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
	 * @return true-完成；false-未完成；
	 */
	public boolean complete() {
		return this.pieces.cardinality() >= this.filePieceSize;
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
	 * <p>将Piece缓存写入文件</p>
	 */
	public void flush() {
		synchronized (this) {
			final var list = new ArrayList<TorrentPiece>();
			this.filePieces.drainTo(list);
			this.flush(list);
		}
	}

	/**
	 * <p>将Piece数据写入文件</p>
	 * 
	 * @param list Piece数据
	 */
	private void flush(List<TorrentPiece> list) {
		if(CollectionUtils.isEmpty(list)) {
			return;
		}
		list.stream().forEach(piece -> this.flush(piece));
	}
	
	/**
	 * <p>将Piece数据写入文件</p>
	 * 
	 * @param piece Piece数据
	 */
	private void flush(TorrentPiece piece) {
		// 判断Piece不在文件范围内
		if(!this.haveIndex(piece.getIndex())) {
			LOGGER.warn("Piece写入文件失败（范围错误）：{}", piece.getIndex());
			return;
		}
		LOGGER.debug("Piece写入文件：{}", piece.getIndex());
		int offset = 0; // 数据偏移
		long seek = 0L; // 文件偏移
		int length = piece.getLength(); // Piece数据长度：计算写入长度
		final long beginPos = piece.beginPos(); // 开始偏移
		final long endPos = piece.endPos(); // 结束偏移
		if(beginPos <= this.fileBeginPos) { // Piece包含文件开始
			offset = (int) (this.fileBeginPos - beginPos);
			length = length - offset;
		} else { // 文件包含Piece开始
			seek = beginPos - this.fileBeginPos;
		}
		if(endPos >= this.fileEndPos) { // Piece包含文件结束
			length = (int) (length - (endPos - this.fileEndPos));
		}
		if(length <= 0) {
			return;
		}
		try {
			this.fileStream.seek(seek); // 注意线程安全
			this.fileStream.write(piece.getData(), offset, length);
		} catch (IOException e) {
			LOGGER.error("Piece写入文件异常", e);
		}
	}
	
	/**
	 * <p>读取缓存中的Piece数据</p>
	 * 
	 * @param index Piece索引
	 * 
	 * @return Piece数据：{@code null}-没有
	 */
	private TorrentPiece torrentPiece(int index) {
		TorrentPiece torrentPiece;
		final var iterator = this.filePieces.iterator();
		while(iterator.hasNext()) {
			torrentPiece = iterator.next();
			if(torrentPiece.getIndex() == index) {
				return torrentPiece;
			}
		}
		return null;
	}
	
	/**
	 * <p>加载文件Piece</p>
	 * <p>第一块Piece索引、最后一块Piece索引、文件Piece数量</p>
	 */
	private void buildFilePiece() {
		this.fileBeginPieceIndex = (int) (this.fileBeginPos / this.pieceLength);
		this.fileEndPieceIndex = (int) (this.fileEndPos / this.pieceLength);
		this.filePieceSize = this.fileEndPieceIndex - this.fileBeginPieceIndex;
		int endPieceSize = (int) (this.fileEndPos % this.pieceLength);
		if(endPieceSize > 0) { // 最后一块包含部分数据
			this.filePieceSize++;
		}
	}
	
	/**
	 * <p>异步加载文件</p>
	 * <p>加载完成{@code sizeCount}计数</p>
	 * <p>任务已经完成：同步加载；反之异步加载；</p>
	 * 
	 * @param complete 任务是否完成
	 * @param sizeCount 文件加载计算器
	 */
	private void buildFileAsyn(boolean complete, CountDownLatch sizeCount) {
		if(complete) { // 同步
			this.buildFile(complete, sizeCount);
		} else { // 异步
			final var lock = this;
			SystemThreadContext.submit(() -> {
				synchronized (lock) {
					this.buildFile(complete, sizeCount);
				}
			});
		}
	}
	
	/**
	 * <p>加载文件</p>
	 * 
	 * @param complete 任务是否完成
	 * @param sizeCount 文件加载计算器
	 */
	private void buildFile(boolean complete, CountDownLatch sizeCount) {
		try {
			this.buildFilePieces(complete);
			this.buildFileDownloadSize();
		} catch (IOException e) {
			LOGGER.error("文件流异步加载异常", e);
		} finally {
			sizeCount.countDown();
		}
	}
	
	/**
	 * <p>加载文件Piece位图</p>
	 * <p>任务没有完成时已下载的Piece需要校验Hash（第一块和最后一块不校验）</p>
	 * 
	 * @param complete 任务是否完成
	 * 
	 * @throws IOException IO异常
	 */
	private void buildFilePieces(boolean complete) throws IOException {
		int pos = 0;
		int length = 0;
		byte[] hash = null;
		byte[] bytes = null;
		boolean verify = true; // 是否校验
		if(this.fileStream.length() == 0) { // 文件还没有开始下载
			return;
		}
		// TODO：优化加载速度
		for (int index = this.fileBeginPieceIndex; index <= this.fileEndPieceIndex; index++) {
			if(complete) { // 任务已经完成
				this.done(index);
				continue;
			}
			if(this.fileInOnePiece()) {
				verify = false;
				pos = this.firstPiecePos();
				length = this.firstPieceSize();
			} else {
				if(index == this.fileBeginPieceIndex) {
					verify = false;
					pos = this.firstPiecePos();
					length = this.firstPieceSize();
				} else if(index == this.fileEndPieceIndex) {
					verify = false;
					pos = 0;
					length = this.lastPieceSize();
				} else {
					verify = true;
					pos = 0;
					length = (int) this.pieceLength;
				}
			}
			bytes = read(index, length, pos, true); // 读取数据
			if(verify) { // 校验Hash
				hash = StringUtils.sha1(bytes);
				if(ArrayUtils.equals(hash, this.torrentStreamGroup.pieceHash(index))) {
					this.done(index);
				}
			} else { // 不校验Hash：验证是否有数据
				if(this.haveData(bytes)) {
					this.done(index);
				}
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
		// 第一块Piece大小
		if(this.havePiece(this.fileBeginPieceIndex)) {
			size += firstPieceSize();
			downloadPieceSize--;
		}
		if(!this.fileInOnePiece()) {
			// 最后一块Piece大小
			if(this.havePiece(this.fileEndPieceIndex)) {
				size += lastPieceSize();
				downloadPieceSize--;
			}
		}
		this.fileDownloadSize.set(size + downloadPieceSize * this.pieceLength);
	}
	
	/**
	 * <p>判断文件是否处于一个Piece之中</p>
	 * 
	 * @return {@code true}-是；{@code false}-不是；
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
			return (int) (this.pieceLength - firstPiecePos());
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
	 * @return {@code true}-含有；{@code false}-不含；
	 */
	private boolean haveData(byte[] bytes) {
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
	 * <p>判断文件是否包含Piece</p>
	 * 
	 * @param index Piece索引
	 * 
	 * @return {@code true}-包含；{@code false}-不包含；
	 */
	private boolean haveIndex(int index) {
		// 不符合当前文件位置
		if(index < this.fileBeginPieceIndex || index > this.fileEndPieceIndex) {
			return false;
		}
		return true;
	}
	
	/**
	 * <p>判断是否含有Piece数据</p>
	 * 
	 * @param index Piece索引
	 * 
	 * @return {@code true}-含有；{@code false}-不含；
	 */
	private boolean havePiece(int index) {
		return this.pieces.get(index);
	}
	
	@Override
	public String toString() {
		return ObjectUtils.toString(this, this.file);
	}

}
