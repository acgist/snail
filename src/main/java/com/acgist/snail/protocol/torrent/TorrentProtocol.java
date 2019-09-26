package com.acgist.snail.protocol.torrent;

import com.acgist.snail.downloader.IDownloader;
import com.acgist.snail.downloader.torrent.TorrentDownloader;
import com.acgist.snail.gui.GuiHandler;
import com.acgist.snail.net.torrent.TorrentManager;
import com.acgist.snail.pojo.session.TaskSession;
import com.acgist.snail.pojo.session.TorrentSession;
import com.acgist.snail.protocol.Protocol;
import com.acgist.snail.protocol.magnet.MagnetProtocol;
import com.acgist.snail.protocol.torrent.bean.Torrent;
import com.acgist.snail.system.config.FileTypeConfig.FileType;
import com.acgist.snail.system.exception.DownloadException;
import com.acgist.snail.utils.FileUtils;

/**
 * BT协议
 * 
 * @author acgist
 * @since 1.0.0
 */
public class TorrentProtocol extends Protocol {

	/**
	 * 种子文件操作
	 */
	public enum TorrentFileOperation {
		
		/** 拷贝 */
		copy,
		/** 移动 */
		move;
		
	}
	
	/**
	 * 种子文件正则表达式
	 */
	public static final String TORRENT_REGEX = ".+\\.torrent";
	/**
	 * 种子文件后缀
	 */
	public static final String TORRENT_SUFFIX = ".torrent";
	
	private String torrentFile; // 种子文件路径
	private TorrentSession torrentSession; // 种子文件信息
	private TorrentFileOperation operation = TorrentFileOperation.copy; // 种子文件操作类型
	
	private static final TorrentProtocol INSTANCE = new TorrentProtocol();
	
	private TorrentProtocol() {
		super(Type.torrent, TORRENT_REGEX);
	}
	
	public static final TorrentProtocol getInstance() {
		return INSTANCE;
	}

	/**
	 * 设置种子文件操作类型
	 */
	public void operation(TorrentFileOperation operation) {
		this.operation = operation;
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
	public IDownloader buildDownloader(TaskSession taskSession) {
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
		this.taskEntity.setFileType(FileType.torrent);
	}
	
	@Override
	protected void buildSize() throws DownloadException {
	}
	
	@Override
	protected void done() throws DownloadException {
		buildTorrentFolder();
		torrentFileOperation();
		selectTorrentFile();
	}
	
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
			throw new DownloadException("BT任务已经存在");
		}
	}
	
	/**
	 * 解析种子，将URL切换为磁力链接
	 */
	private void torrent() throws DownloadException {
		final String torrentFile = this.url;
		final TorrentSession torrentSession = TorrentManager.getInstance().newTorrentSession(torrentFile);
		this.url = MagnetProtocol.buildMagnet(torrentSession.infoHash().infoHashHex()); // 生成磁力链接
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
		if(this.operation == TorrentFileOperation.move) {
			FileUtils.move(this.torrentFile, newFilePath);
		} else {
			FileUtils.copy(this.torrentFile, newFilePath);
		}
		this.taskEntity.setTorrent(newFilePath);
	}

	/**
	 * 选择torrent下载文件和设置文件大小
	 * 
	 * @return true-已选择下载文件；false-未选择下载文件
	 */
	private void selectTorrentFile() throws DownloadException {
		final TaskSession taskSession = TaskSession.newInstance(this.taskEntity);
		GuiHandler.getInstance().torrent(taskSession); // 不能抛出异常
		if(taskSession.downloadTorrentFiles().isEmpty()) { // 没有选择下载文件
			FileUtils.delete(this.taskEntity.getFile());
			throw new DownloadException("请选择下载文件");
		}
	}
	
}
