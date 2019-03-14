package com.acgist.snail.protocol.torrent;

import java.io.File;

import com.acgist.snail.gui.torrent.TorrentWindow;
import com.acgist.snail.pojo.entity.TaskEntity;
import com.acgist.snail.pojo.entity.TaskEntity.Status;
import com.acgist.snail.pojo.entity.TaskEntity.Type;
import com.acgist.snail.pojo.wrapper.TaskWrapper;
import com.acgist.snail.pojo.wrapper.TorrentWrapper;
import com.acgist.snail.protocol.Protocol;
import com.acgist.snail.protocol.magnet.MagnetCoder;
import com.acgist.snail.system.config.FileTypeConfig.FileType;
import com.acgist.snail.system.exception.DownloadException;
import com.acgist.snail.utils.FileUtils;

public class TorrentProtocol extends Protocol {

	public static final String TORRENT_SUFFIX = ".torrent"; // 文件后缀
	public static final String TORRENT_REGEX = ".+\\.torrent"; // 正则表达式
	
	private String torrent; // 种子文件路径
	private TorrentWrapper torrentWrapper; // 种子文件信息
	
	private static final TorrentProtocol INSTANCE = new TorrentProtocol();
	
	public TorrentProtocol() {
		super(Type.torrent, TORRENT_REGEX);
	}
	
	public static final TorrentProtocol getInstance() {
		return INSTANCE;
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
		boolean ok = true;
		torrent();
		TaskEntity taskEntity = new TaskEntity();
		String fileName = buildFileName(); // 文件名称
		taskEntity.setUrl(this.url);
		taskEntity.setType(this.type);
		taskEntity.setStatus(Status.await);
		taskEntity.setName(fileName);
		taskEntity.setFile(buildFile(fileName));
		taskEntity.setFileType(FileType.torrent);
		this.taskEntity = taskEntity;
		buildTorrentFolder();
		moveTorrentFile();
		ok = selectTorrentFile();
		return ok;
	}

	@Override
	protected void cleanMessage() {
		this.torrent = null;
		this.torrentWrapper = null;
	}
	
	@Override
	protected String buildFileName() {
		return torrentWrapper.name();
	}

	/**
	 * 解析种子
	 */
	private void torrent() throws DownloadException {
		this.torrent = this.url;
		String url = this.url;
		TorrentCoder decoder = TorrentCoder.newInstance(url);
		this.url = MagnetCoder.buildMagnet(decoder.hash());
		this.torrentWrapper = decoder.torrentWrapper();
	}
	
	/**
	 * 创建下载目录
	 */
	private void buildTorrentFolder() {
		File folder = new File(this.taskEntity.getFile());
		if(!folder.exists()) {
			folder.mkdirs();
		}
	}
	
	/**
	 * 移动下载种子文件到下载目录
	 */
	private void moveTorrentFile() {
		String fileName = FileUtils.fileNameFromUrl(this.torrent);
		String newFilePath = FileUtils.file(this.taskEntity.getFile(), fileName);
		FileUtils.copy(this.torrent, newFilePath);
		this.taskEntity.setTorrent(newFilePath);
	}
	
	/**
	 * 选择torrent下载文件和设置文件大小
	 */
	private boolean selectTorrentFile() throws DownloadException {
		TaskWrapper taskWrapper = TaskWrapper.newInstance(this.taskEntity);
		TorrentWindow.getInstance().show(taskWrapper);
		if(taskWrapper.downloadTorrentFiles().isEmpty()) { // 没有选择下载文件
			FileUtils.delete(this.taskEntity.getFile());
			return false;
		}
		return true;
	}
	
}
