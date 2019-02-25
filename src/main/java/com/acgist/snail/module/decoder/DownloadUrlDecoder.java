package com.acgist.snail.module.decoder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.acgist.snail.module.config.DownloadConfig;
import com.acgist.snail.module.config.FileTypeConfig.FileType;
import com.acgist.snail.module.exception.DownloadException;
import com.acgist.snail.pojo.entity.TaskEntity;
import com.acgist.snail.pojo.entity.TaskEntity.Status;
import com.acgist.snail.pojo.entity.TaskEntity.Type;
import com.acgist.snail.utils.FileUtils;

/**
 * 下载地址转换
 */
public class DownloadUrlDecoder {

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
		TYPE.put(Type.torrent, List.of(
			".+\\.torrent", "magnet:\\?xt=urn:btih:.+", "thunder://.+", "[a-zA-Z0-9]{40}"
		));
	}
	
	private String url;
	private Type type;
	private String fileName;
	private FileType fileType;
	private String file;
	private String name;
	private String torrent;
	private Integer size;
	
	private DownloadUrlDecoder(String url) {
		this.url = url;
	}
	
	public static final DownloadUrlDecoder newDecoder(String url) {
		return new DownloadUrlDecoder(url);
	}
	
	/**
	 * 新建任务实体：url、name、type、fileType、file、torrent、size	
	 */
	public TaskEntity buildTaskEntity() throws DownloadException {
		pretreatment();
		TaskEntity entity = new TaskEntity();
		entity.setUrl(this.url);
		entity.setStatus(Status.await);
		entity.setType(this.type);
		entity.setFileType(this.fileType);
		entity.setFile(this.file);
		entity.setName(this.name);
		entity.setTorrent(torrent);
		entity.setSize(size);
		return entity;
	}

	/**
	 * 预处理
	 */
	private void pretreatment() throws DownloadException {
		type();
		fileName();
		fileType();
		file();
		name();
		torrent();
		size();
	}
	
	/**
	 * 获取下载类型
	 */
	private void type() throws DownloadException {
		String url = this.url;
		Optional<Entry<Type, List<String>>> optional = TYPE.entrySet().stream().filter((entity) -> {
			return entity.getValue().stream().filter(value -> {
				Pattern pattern = Pattern.compile(value, Pattern.CASE_INSENSITIVE);
				Matcher matcher = pattern.matcher(url);
				return matcher.matches();
			}).count() > 0;
		}).findFirst();
		if(optional.isEmpty()) {
			throw new DownloadException("不支持的下载协议：" + url);
		}
		this.type = optional.get().getKey();
	}

	/**
	 * 获取下载名称：
	 * 	ftp：文件名
	 * 	http：文件名
	 * 	torrent：任务名
	 */
	private void fileName() {
		String url = this.url;
		String fileName = null;
		switch (this.type) {
			case ftp:
				fileName = FileUtils.fileNameFromUrl(url);
				break;
			case http:
				fileName = FileUtils.fileNameFromHttp(url);
				break;
			case torrent:
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
	private void file() {
		String downloadPath = DownloadConfig.getDownloadPath();
		this.file = downloadPath + "/" + this.fileName;
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
	 * 种子文件
	 */
	private void torrent() {
	}
	
	/**
	 * 文件大小：Content-Range、content-length
	 */
	private void size() {
		
	}
	
}
