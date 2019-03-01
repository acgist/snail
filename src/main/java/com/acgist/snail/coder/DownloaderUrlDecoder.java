package com.acgist.snail.coder;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.acgist.snail.coder.magnet.MagnetDecoder;
import com.acgist.snail.coder.thunder.ThunderDecoder;
import com.acgist.snail.coder.torrent.TorrentDecoder;
import com.acgist.snail.module.config.DownloadConfig;
import com.acgist.snail.module.config.FileTypeConfig.FileType;
import com.acgist.snail.module.exception.DownloadException;
import com.acgist.snail.pojo.entity.TaskEntity;
import com.acgist.snail.pojo.entity.TaskEntity.Status;
import com.acgist.snail.pojo.entity.TaskEntity.Type;
import com.acgist.snail.pojo.wrapper.TaskWrapper;
import com.acgist.snail.pojo.wrapper.TorrentWrapper;
import com.acgist.snail.repository.impl.TaskRepository;
import com.acgist.snail.utils.FileUtils;
import com.acgist.snail.window.torrent.TorrentWindow;

/**
 * 下载地址转换：
 * ftp、http：单文件下载
 * torrent：下载目录为文件夹，文件夹里面包含下载文件
 */
public class DownloaderUrlDecoder {

	/**
	 * 下载类型获取
	 */
	private static final Map<Type, List<String>> TYPE = new HashMap<>();
	
	static {
		TYPE.put(Type.ftp, List.of(
			"ftp://.+"
		));
		TYPE.put(Type.http, List.of(
			"http://.+", "https://.+"
		));
		TYPE.put(Type.ed2k, List.of(
			"ed2k://\\|file\\|.+"
		));
		TYPE.put(Type.torrent, List.of(
			".+\\.torrent", "magnet:\\?xt=urn:btih:.+", "[a-zA-Z0-9]{40}"
		));
	}
	
	private boolean magnet = false; // 磁力链接
	private String url; // 下载地址
	private Type type; // 下载类型
	private String fileName; // 文件名称
	private FileType fileType; // 下载文件类型
	private String file; // 下载文件
	private String name; // 任务名称
	private String torrent; // 种子文件
	private Integer size; // 大小
	private TaskWrapper taskWrapper;
	private TorrentWrapper torrentWrapper;
	
	private DownloaderUrlDecoder(String url) {
		this.url = url;
	}
	
	public static final DownloaderUrlDecoder newDecoder(String url) {
		return new DownloaderUrlDecoder(url);
	}
	
	/**
	 * 新建任务实体：url、name、type、fileType、file、torrent、size	
	 */
	public TaskWrapper buildTaskWrapper() throws DownloadException {
		pretreatment();
		buildMessage();
		buildWrapper();
		buildTask();
		selectTorrentFile();
		return taskWrapper;
	}

	/**
	 * 新建信息
	 */
	private void buildMessage() throws DownloadException {
		type();
		torrent();
		fileName();
		fileType();
		file();
		name();
		size();
	}

	private void buildWrapper() {
		TaskEntity entity = new TaskEntity();
		entity.setUrl(this.url);
		entity.setStatus(Status.await);
		entity.setType(this.type);
		entity.setFileType(this.fileType);
		entity.setFile(this.file);
		entity.setName(this.name);
		entity.setTorrent(torrent);
		entity.setSize(size);
		this.taskWrapper = new TaskWrapper(entity);
	}

	/**
	 * 预处理：去空格、格式转换
	 */
	private void pretreatment() {
		String url = this.url.trim();
		if(ThunderDecoder.verify(url)) {
			url = ThunderDecoder.decode(url);
		}
		this.url = url;
	}
	
	/**
	 * 获取下载类型
	 */
	private void type() throws DownloadException {
		String url = this.url;
		Optional<Entry<Type, List<String>>> optional = TYPE.entrySet()
			.stream()
			.filter(entry -> {
				return entry.getValue()
					.stream()
					.anyMatch(value -> {
						Pattern pattern = Pattern.compile(value, Pattern.CASE_INSENSITIVE);
						Matcher matcher = pattern.matcher(url);
						return matcher.matches();
					});
			}).findFirst(); // 如果多个选择第一个
		if(optional.isEmpty()) {
			throw new DownloadException("不支持的下载协议：" + url);
		}
		this.type = optional.get().getKey();
	}

	/**
	 * 磁力链接：下载种子->解析种子
	 * 种子文件：解析种子
	 */
	private void torrent() throws DownloadException {
		if(this.type == Type.torrent) {
			String url = this.url;
			TorrentDecoder decoder = null;
			if(MagnetDecoder.verify(url)) { // 磁力链接
				File file = MagnetDecoder.download(url);
				if(file == null) {
					throw new DownloadException("下载种子失败：" + url);
				}
				this.torrent = file.getPath();
				decoder = TorrentDecoder.newInstance(this.torrent);
				magnet = true;
			} else {
				decoder = TorrentDecoder.newInstance(url);
				this.url = MagnetDecoder.buildMagnet(decoder.hash());
				this.torrent = url;
			}
			torrentWrapper = decoder.torrentWrapper();
		}
	}
	
	/**
	 * 获取下载名称：
	 * 	ftp：文件名
	 * 	http：文件名
	 * 	torrent：任务名
	 */
	private void fileName() throws DownloadException {
		String url = this.url;
		String fileName = null;
		switch (this.type) {
			case ftp:
				fileName = FileUtils.fileNameFromUrl(url);
				break;
			case http:
				fileName = FileUtils.fileNameFromHttp(url);
				break;
			case ed2k:
				throw new DownloadException("暂时不支持的协议");
			case torrent:
				fileName = this.torrentWrapper.name();
				break;
		}
		fileName = FileUtils.fileName(fileName);
		this.fileName = fileName;
	}
	
	/**
	 * 下载文件类型
	 */
	private void fileType() {
		if(this.type == Type.torrent) {
			this.fileType = FileType.torrent;
		} else {
			this.fileType = FileUtils.fileType(this.fileName);
		}
	}
	
	/**
	 * 设置下载文件地址
	 */
	private void file() throws DownloadException {
		this.file = DownloadConfig.getDownloadPath(this.fileName);
		if(this.type == Type.torrent) {
			File folder = new File(this.file);
			if(!folder.exists()) {
				folder.mkdirs();
			}
		}
	}
	
	/**
	 * 任务名称
	 */
	private void name() {
		if(this.type == Type.torrent) {
			this.name = this.fileName;
		} else {
			int index = this.fileName.lastIndexOf(".");
			if(index != -1) {
				this.name = this.fileName.substring(0, index);
			} else {
				this.name = this.fileName;
			}
		}
	}
	
	/**
	 * 文件大小：Content-Range、content-length
	 */
	private void size() {
	}
	
	/**
	 * 生成其他信息
	 */
	private void buildTask() {
		if(this.type == Type.torrent) {
			String fileName = FileUtils.fileNameFromUrl(this.torrent);
			String newFilePath = FileUtils.file(this.file, fileName);
			if(magnet) { // 磁力链接需要移动种子文件
				File torrentFile = new File(this.torrent);
				File newFile = new File(newFilePath);
				torrentFile.renameTo(newFile);
			} else {
				FileUtils.copy(this.torrent, newFilePath);
			}
			this.taskWrapper.setTorrent(newFilePath);
		}
		TaskRepository repository = new TaskRepository();
		repository.save(this.taskWrapper.getEntity());
	}
	
	/**
	 * BT下载选择下载文件
	 */
	private void selectTorrentFile() {
		TorrentWindow.getInstance().show(this.taskWrapper);
	}
	
}
