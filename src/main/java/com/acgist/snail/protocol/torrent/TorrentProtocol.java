package com.acgist.snail.protocol.torrent;

import java.io.File;

import com.acgist.snail.downloader.IDownloader;
import com.acgist.snail.gui.torrent.TorrentWindow;
import com.acgist.snail.pojo.entity.TaskEntity;
import com.acgist.snail.pojo.entity.TaskEntity.Status;
import com.acgist.snail.pojo.entity.TaskEntity.Type;
import com.acgist.snail.pojo.wrapper.TorrentWrapper;
import com.acgist.snail.protocol.Protocol;
import com.acgist.snail.protocol.magnet.MagnetDecoder;
import com.acgist.snail.system.config.FileTypeConfig.FileType;
import com.acgist.snail.system.exception.DownloadException;
import com.acgist.snail.utils.FileUtils;

public class TorrentProtocol extends Protocol {

	public static final String TORRENT_SUFFIX = ".torrent"; // 文件后缀
	public static final String TORRENT_REGEX = ".+\\.torrent"; // 正则表达式
	
	private String torrent; // 种子文件路径
	private TorrentWrapper torrentWrapper; // 种子文件信息
	
	public TorrentProtocol() {
		super(Type.torrent, TORRENT_REGEX);
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
	protected void buildTaskEntity() throws DownloadException {
		torrent();
		TaskEntity taskEntity = new TaskEntity();
		String fileName = buildFileName(); // 文件名称
		taskEntity.setUrl(this.url);
		taskEntity.setType(this.type);
		taskEntity.setStatus(Status.await);
		taskEntity.setName(torrentWrapper.name());
		taskEntity.setFile(buildFile(fileName));
		taskEntity.setFileType(FileType.torrent);
		buildTorrentFolder();
		moveTorrentFile();
		selectTorrentFile();
		this.taskEntity = taskEntity;
	}

	@Override
	protected IDownloader buildDownloader() {
		return null;
	}

	@Override
	protected void cleanMessage() {
		this.torrent = null;
		this.torrentWrapper = null;
	}

	/**
	 * 解析种子
	 */
	private void torrent() throws DownloadException {
		this.torrent = this.url;
		String url = this.url;
		TorrentDecoder decoder = TorrentDecoder.newInstance(url);
		this.url = MagnetDecoder.buildMagnet(decoder.hash());
		this.torrentWrapper = decoder.torrentWrapper();
	}
	
	/**
	 * 创建下载目录
	 */
	private void buildTorrentFolder() {
		File folder = new File(taskEntity.getFile());
		if(!folder.exists()) {
			folder.mkdirs();
		}
	}
	
	/**
	 * 移动下载种子文件到下载目录
	 */
	private void moveTorrentFile() {
		String fileName = FileUtils.fileNameFromUrl(this.torrent);
		String newFilePath = FileUtils.file(taskEntity.getFile(), fileName);
		FileUtils.copy(this.torrent, newFilePath);
		this.taskEntity.setTorrent(newFilePath);
	}
	
	/**
	 * 选择torrent下载文件和设置文件大小
	 */
	private void selectTorrentFile() {
		TorrentWindow.getInstance().show(this.taskWrapper);
		if(this.taskWrapper.downloadTorrentFiles().isEmpty()) {
			FileUtils.delete(this.taskEntity.getFile());
			this.taskEntity = null;
			this.taskWrapper = null;
		}
	}
	
}
