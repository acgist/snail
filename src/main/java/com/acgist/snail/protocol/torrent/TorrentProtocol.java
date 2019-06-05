package com.acgist.snail.protocol.torrent;

import com.acgist.snail.gui.torrent.TorrentWindow;
import com.acgist.snail.pojo.entity.TaskEntity;
import com.acgist.snail.pojo.entity.TaskEntity.Status;
import com.acgist.snail.pojo.entity.TaskEntity.Type;
import com.acgist.snail.pojo.session.TaskSession;
import com.acgist.snail.pojo.session.TorrentSession;
import com.acgist.snail.protocol.Protocol;
import com.acgist.snail.protocol.magnet.MagnetProtocol;
import com.acgist.snail.system.config.FileTypeConfig.FileType;
import com.acgist.snail.system.exception.DownloadException;
import com.acgist.snail.system.manager.TorrentManager;
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
		copy, // 拷贝
		move; // 移动
	}
	
	public static final String TORRENT_SUFFIX = ".torrent"; // 文件后缀
	
	public static final String TORRENT_REGEX = ".+\\.torrent"; // 正则表达式
	
	private String torrent; // 种子文件路径
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
		return "BT";
	}

	@Override
	public boolean available() {
		return true;
	}

	@Override
	protected boolean buildTaskEntity() throws DownloadException {
		torrent();
		final TaskEntity taskEntity = new TaskEntity();
		final String fileName = buildFileName(); // 文件名称
		taskEntity.setUrl(this.url);
		taskEntity.setType(this.type);
		taskEntity.setStatus(Status.await);
		taskEntity.setName(fileName);
		taskEntity.setFile(buildFile(fileName));
		taskEntity.setFileType(FileType.torrent);
		this.taskEntity = taskEntity;
		buildTorrentFolder();
		torrentFileOperation();
		return selectTorrentFile();
	}

	@Override
	protected void cleanMessage() {
		this.torrent = null;
		this.torrentSession = null;
	}
	
	@Override
	protected String buildFileName() {
		return this.torrentSession.name();
	}

	/**
	 * 解析种子
	 */
	private void torrent() throws DownloadException {
		final String torrentFile = this.url;
		final TorrentSession torrentSession = TorrentManager.getInstance().newTorrentSession(torrentFile);
		this.url = MagnetProtocol.buildMagnet(torrentSession.infoHash().infoHashHex()); // 生成磁力链接
		this.torrent = torrentFile;
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
		final String fileName = FileUtils.fileNameFromUrl(this.torrent);
		final String newFilePath = FileUtils.file(this.taskEntity.getFile(), fileName);
		if(this.operation == TorrentFileOperation.move) {
			FileUtils.move(this.torrent, newFilePath);
		} else {
			FileUtils.copy(this.torrent, newFilePath);
		}
		this.taskEntity.setTorrent(newFilePath);
	}

	/**
	 * 选择torrent下载文件和设置文件大小
	 * 
	 * @return true-已选择下载文件；false-未选择下载文件
	 */
	private boolean selectTorrentFile() throws DownloadException {
		final TaskSession taskSession = TaskSession.newInstance(this.taskEntity);
		TorrentWindow.getInstance().show(taskSession);
		if(taskSession.downloadTorrentFiles().isEmpty()) { // 没有选择下载文件
			FileUtils.delete(this.taskEntity.getFile());
			return false;
		}
		return true;
	}
	
}
