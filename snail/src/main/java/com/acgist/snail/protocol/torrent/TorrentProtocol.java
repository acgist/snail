package com.acgist.snail.protocol.torrent;

import com.acgist.snail.context.GuiContext;
import com.acgist.snail.context.TorrentContext;
import com.acgist.snail.context.exception.DownloadException;
import com.acgist.snail.downloader.IDownloader;
import com.acgist.snail.downloader.torrent.TorrentDownloader;
import com.acgist.snail.pojo.ITaskSession;
import com.acgist.snail.pojo.ITaskSession.FileType;
import com.acgist.snail.pojo.Torrent;
import com.acgist.snail.pojo.session.TaskSession;
import com.acgist.snail.pojo.session.TorrentSession;
import com.acgist.snail.protocol.Protocol;
import com.acgist.snail.utils.FileUtils;

/**
 * <p>BT协议</p>
 * 
 * @author acgist
 */
public final class TorrentProtocol extends Protocol {
	
	private static final TorrentProtocol INSTANCE = new TorrentProtocol();
	
	public static final TorrentProtocol getInstance() {
		return INSTANCE;
	}

	/**
	 * <p>种子文件操作类型</p>
	 * 
	 * @author acgist
	 */
	public enum TorrentHandle {
		
		/**
		 * <p>拷贝：拷贝种子文件到下载目录（源文件不变）</p>
		 */
		COPY,
		/**
		 * <p>移动：移动种子文件到下载目录（源文件删除）</p>
		 */
		MOVE;
		
	}
	
	/**
	 * <p>种子文件路径</p>
	 */
	private String torrentFile;
	/**
	 * <p>种子信息</p>
	 */
	private TorrentSession torrentSession;
	/**
	 * <p>种子文件操作类型</p>
	 */
	private TorrentHandle handle = TorrentHandle.COPY;
	
	private TorrentProtocol() {
		super(Type.TORRENT, "BitTorrent");
	}

	/**
	 * <p>设置种子文件操作类型</p>
	 * 
	 * @param handle 种子文件操作
	 */
	public void torrentHandle(TorrentHandle handle) {
		this.handle = handle;
	}
	
	@Override
	public boolean available() {
		return true;
	}
	
	@Override
	public IDownloader buildDownloader(ITaskSession taskSession) {
		return TorrentDownloader.newInstance(taskSession);
	}

	@Override
	protected void prep() throws DownloadException {
		this.checkExist();
		this.torrent();
	}

	@Override
	protected String buildFileName() {
		return this.torrentSession.name();
	}

	@Override
	protected void buildName(String fileName) {
		this.taskEntity.setName(fileName);
	}
	
	@Override
	protected void buildFileType(String fileName) {
		this.taskEntity.setFileType(FileType.TORRENT);
	}
	
	@Override
	protected void buildSize() throws DownloadException {
		// 设置选择下载文件时计算大小
	}
	
	@Override
	protected void done() throws DownloadException {
		this.buildFolder();
		this.torrentHandle();
		this.selectFiles();
	}
	
	@Override
	protected void release(boolean success) {
		if(!success && this.torrentSession != null) {
			// 删除新建文件
			FileUtils.delete(this.taskEntity.getFile());
			// 清除种子信息
			TorrentContext.getInstance().remove(this.torrentSession.infoHashHex());
		}
		this.torrentFile = null;
		this.torrentSession = null;
		super.release(success);
	}
	
	/**
	 * <p>检查任务是否已经存在</p>
	 * <p>一定要先检查BT任务是否已经存在（如果已经存在不能赋值：防止清除下载任务）</p>
	 * 
	 * @throws DownloadException 下载异常
	 */
	private void checkExist() throws DownloadException {
		final Torrent torrent = TorrentContext.loadTorrent(this.url);
		if(TorrentContext.getInstance().exist(torrent.infoHash().infoHashHex())) {
			throw new DownloadException("任务已经存在");
		}
	}
	
	/**
	 * <p>解析种子</p>
	 * <p>转换磁力链接、生成种子信息</p>
	 * 
	 * @throws DownloadException 下载异常
	 */
	private void torrent() throws DownloadException {
		this.torrentFile = this.url;
		this.torrentSession = TorrentContext.getInstance().newTorrentSession(this.torrentFile);
		this.url = Protocol.Type.buildMagnet(this.torrentSession.infoHash().infoHashHex());
	}
	
	/**
	 * <p>新建下载目录</p>
	 */
	private void buildFolder() {
		FileUtils.buildFolder(this.taskEntity.getFile());
	}

	/**
	 * <p>种子文件操作：拷贝、移动</p>
	 */
	private void torrentHandle() {
		final String fileName = FileUtils.fileName(this.torrentFile);
		final String newFilePath = FileUtils.file(this.taskEntity.getFile(), fileName);
		if(this.handle == TorrentHandle.MOVE) {
			FileUtils.move(this.torrentFile, newFilePath);
		} else {
			FileUtils.copy(this.torrentFile, newFilePath);
		}
		this.taskEntity.setTorrent(newFilePath);
	}

	/**
	 * <p>选择下载文件、设置文件大小</p>
	 * 
	 * @throws DownloadException 下载异常
	 */
	private void selectFiles() throws DownloadException {
		ITaskSession taskSession = null;
		try {
			taskSession = TaskSession.newInstance(this.taskEntity);
			GuiContext.getInstance().multifile(taskSession);
		} catch (DownloadException e) {
			throw e;
		} catch (Exception e) {
			throw new DownloadException("选择下载文件错误", e);
		}
		if(taskSession.multifileSelected().isEmpty()) {
			throw new DownloadException("没有选择下载文件");
		}
	}
	
}
