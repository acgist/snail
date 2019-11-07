package com.acgist.snail.protocol.torrent;

import com.acgist.snail.downloader.IDownloader;
import com.acgist.snail.downloader.torrent.TorrentDownloader;
import com.acgist.snail.gui.GuiHandler;
import com.acgist.snail.net.torrent.TorrentManager;
import com.acgist.snail.pojo.ITaskSession;
import com.acgist.snail.pojo.ITaskSession.FileType;
import com.acgist.snail.pojo.bean.Torrent;
import com.acgist.snail.pojo.session.TaskSession;
import com.acgist.snail.pojo.session.TorrentSession;
import com.acgist.snail.protocol.Protocol;
import com.acgist.snail.system.exception.DownloadException;
import com.acgist.snail.utils.FileUtils;

/**
 * BT协议
 * 
 * @author acgist
 * @since 1.0.0
 */
public final class TorrentProtocol extends Protocol {
	
	private static final TorrentProtocol INSTANCE = new TorrentProtocol();

	/**
	 * 种子文件操作类型
	 */
	public enum TorrentHandle {
		
		/** 拷贝：拷贝种子文件到下载目录（源文件不变） */
		COPY,
		/** 移动：移动种子文件到下载目录（源文件删除） */
		MOVE;
		
	}
	
	/**
	 * 种子文件路径
	 */
	private String torrentFile;
	/**
	 * 种子信息
	 */
	private TorrentSession torrentSession;
	/**
	 * 种子文件操作类型
	 */
	private TorrentHandle handle = TorrentHandle.COPY;
	
	private TorrentProtocol() {
		super(Type.TORRENT);
	}
	
	public static final TorrentProtocol getInstance() {
		return INSTANCE;
	}

	public void operation(TorrentHandle operation) {
		this.handle = operation;
	}
	
	@Override
	public String name() {
		return "BitTorrent";
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
		exist();
		torrent();
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
	}
	
	@Override
	protected void done() throws DownloadException {
		buildTorrentFolder();
		torrentFileOperation();
		selectTorrentFiles();
	}
	
	/**
	 * {@inheritDoc}
	 * <p>注意：一定先检查BT任务是否已经存在，如果已经存在不能赋值，失败后清除。</p>
	 */
	@Override
	protected void cleanMessage(boolean ok) {
		if(!ok) { // 失败
			if(this.torrentSession != null) {
				TorrentManager.getInstance().remove(this.torrentSession.infoHashHex());
			}
		}
		this.torrentFile = null;
		this.torrentSession = null;
	}
	
	private void exist() throws DownloadException {
		final Torrent torrent = TorrentManager.loadTorrent(this.url);
		if(TorrentManager.getInstance().exist(torrent.getInfoHash().infoHashHex())) {
			throw new DownloadException("任务已经存在");
		}
	}
	
	/**
	 * <dl>
	 * 	<dt>解析种子</dt>
	 * 	<dd>种子文件地址转换为磁力链接</dd>
	 * 	<dd>生成种子信息</dd>
	 * </dl>
	 */
	private void torrent() throws DownloadException {
		final String torrentFile = this.url;
		final TorrentSession torrentSession = TorrentManager.getInstance().newTorrentSession(torrentFile);
		this.url = Protocol.Type.buildMagnet(torrentSession.infoHash().infoHashHex()); // 生成磁力链接
		this.torrentFile = torrentFile;
		this.torrentSession = torrentSession;
	}
	
	/**
	 * 创建下载目录
	 */
	private void buildTorrentFolder() {
		FileUtils.buildFolder(this.taskEntity.getFile(), false);
	}

	/**
	 * 种子文件操作：拷贝、移动
	 */
	private void torrentFileOperation() {
		final String fileName = FileUtils.fileNameFromUrl(this.torrentFile);
		final String newFilePath = FileUtils.file(this.taskEntity.getFile(), fileName);
		if(this.handle == TorrentHandle.MOVE) {
			FileUtils.move(this.torrentFile, newFilePath);
		} else {
			FileUtils.copy(this.torrentFile, newFilePath);
		}
		this.taskEntity.setTorrent(newFilePath);
	}

	/**
	 * 选择torrent下载文件和设置文件大小
	 */
	private void selectTorrentFiles() throws DownloadException {
		final ITaskSession taskSession = TaskSession.newInstance(this.taskEntity);
		GuiHandler.getInstance().torrent(taskSession); // 不能抛出异常
		if(taskSession.selectTorrentFiles().isEmpty()) { // 没有选择下载文件
			FileUtils.delete(this.taskEntity.getFile());
			throw new DownloadException("请选择下载文件");
		}
	}
	
}
