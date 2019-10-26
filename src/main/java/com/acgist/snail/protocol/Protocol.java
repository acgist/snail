package com.acgist.snail.protocol;

import java.io.File;

import com.acgist.snail.downloader.IDownloader;
import com.acgist.snail.pojo.entity.TaskEntity;
import com.acgist.snail.pojo.session.TaskSession;
import com.acgist.snail.pojo.session.TaskSession.Status;
import com.acgist.snail.repository.impl.TaskRepository;
import com.acgist.snail.system.config.DownloadConfig;
import com.acgist.snail.system.exception.DownloadException;
import com.acgist.snail.utils.FileUtils;
import com.acgist.snail.utils.StringUtils;

/***
 * <p>下载协议</p>
 * <p>非线程安全</p>
 * 
 * @author acgist
 * @since 1.0.0
 */
public abstract class Protocol {

	/**
	 * 协议类型
	 */
	public enum Type {

		/** ws、wss：websocket */
		WS(
			new String[] {"ws://.+", "wss://.+"},
			new String[] {"ws://", "wss://"},
			new String[] {},
			"ws://",
			""
		),
		/** udp */
		UDP(
			new String[] {"udp://.+"},
			new String[] {"udp://"},
			new String[] {},
			"udp://",
			""
		),
		/** tcp */
		TCP(
			new String[] {"tcp://.+"},
			new String[] {"tcp://"},
			new String[] {},
			"tcp://",
			""
		),
		/** ftp */
		FTP(
			new String[] {"ftp://.+"},
			new String[] {"ftp://"},
			new String[] {},
			"ftp://",
			""
		),
		/** http、https */
		HTTP(
			new String[] {"http://.+", "https://.+"},
			new String[] {"http://", "https://"},
			new String[] {},
			"http://",
			""
		),
		/** 磁力链接 */
		MAGNET(
			// 注意顺序：后面需要验证磁力链接的具体格式
			new String[] {"magnet:\\?.+", "[a-zA-Z0-9]{32}", "[a-zA-Z0-9]{40}"},
			new String[] {"magnet:?xt=urn:btih:"},
			new String[] {},
			"magnet:?xt=urn:btih:",
			""
		),
		/** 迅雷链接 */
		THUNDER(
			new String[] {"thunder://.+"},
			new String[] {"thunder://"},
			new String[] {},
			"thunder://",
			""
		),
		/** BT */
		TORRENT(
			new String[] {".+\\.torrent"},
			new String[] {},
			new String[] {".torrent"},
			"",
			".torrent"
		);
		
		/**
		 * 正则表达式
		 */
		private String[] regexs;
		/**
		 * 前缀
		 */
		private String[] prefix;
		/**
		 * 后缀
		 */
		private String[] suffix;
		/**
		 * 默认前缀
		 */
		private String defaultPrefix;
		/**
		 * 默认后缀
		 */
		private String defaultSuffix;
		
		private Type(String[] regexs, String[] prefix, String[] suffix, String defaultPrefix, String defaultSuffix) {
			this.regexs = regexs;
			this.prefix = prefix;
			this.suffix = suffix;
			this.defaultPrefix = defaultPrefix;
			this.defaultSuffix = defaultSuffix;
		}

		/**
		 * @return 所有的正则表达式
		 */
		public String[] regexs() {
			return this.regexs;
		}

		/**
		 * @return 所有的前缀
		 */
		public String[] prefix() {
			return this.prefix;
		}
		
		/**
		 * @param url 链接
		 * @return 符合链接的前缀
		 */
		public String prefix(String url) {
			for (String value : this.prefix) {
				if(StringUtils.startsWith(url, value)) {
					return value;
				}
			}
			return null;
		}
		
		/**
		 * @return 所有的后缀
		 */
		public String[] suffix() {
			return this.suffix;
		}
		
		/**
		 * @param url 链接
		 * @return 符合链接的后缀
		 */
		public String suffix(String url) {
			for (String value : this.suffix) {
				if(StringUtils.endsWith(url, value)) {
					return value;
				}
			}
			return null;
		}
		
		/**
		 * @return 默认前缀
		 */
		public String defaultPrefix() {
			return this.defaultPrefix;
		}
		
		/**
		 * @return 默认后缀
		 */
		public String defaultSuffix() {
			return this.defaultSuffix;
		}
		
		/**
		 * 验证协议
		 * 
		 * @param url 链接
		 * 
		 * @return true：属于；false：不属于；
		 */
		public boolean verify(String url) {
			for (String regex : this.regexs) {
				final boolean match = StringUtils.regex(url, regex, true);
				if(match) {
					return true;
				}
			}
			return false;
		}
		
		/**
		 * 将HASH转为磁力链接（完整链接）
		 */
		public static final String buildMagnet(String hash) {
			if(verifyMagnet(hash)) {
				return hash;
			}
			return Type.MAGNET.defaultPrefix + hash.toLowerCase();
		}
		
		/**
		 * 验证磁力链接（完整链接）
		 */
		public static final boolean verifyMagnet(String url) {
			return StringUtils.regex(url, Type.MAGNET.regexs[0], true);
		}
		
		/**
		 * 验证32位磁力链接HASH
		 */
		public static final boolean verifyMagnetHash32(String url) {
			return StringUtils.regex(url, Type.MAGNET.regexs[1], true);
		}
		
		/**
		 * 验证40位磁力链接HASH
		 */
		public static final boolean verifyMagnetHash40(String url) {
			return StringUtils.regex(url, Type.MAGNET.regexs[2], true);
		}
		
	}
	
	/**
	 * 下载任务类型
	 */
	protected final Type type;
	/**
	 * 下载地址
	 */
	protected String url;
	/**
	 * 下载任务
	 */
	protected TaskEntity taskEntity;
	
	public Protocol(Type type) {
		this.type = type;
	}

	/**
	 * 初始化
	 */
	public Protocol init(String url) {
		this.url = url.trim();
		return this;
	}
	
	/**
	 * 协议类型
	 * 
	 * @return 协议类型
	 */
	public Type type() {
		return this.type;
	}
	
	/**
	 * 协议名称
	 * 
	 * @return 协议名称
	 */
	public abstract String name();
	
	/**
	 * 验证是否支持协议
	 */
	public boolean verify() {
		if(this.type == null) {
			return false;
		}
		return this.type.verify(this.url);
	}
	
	/**
	 * 是否可用
	 * 
	 * @return 是否可用
	 */
	public abstract boolean available();
	
	/**
	 * 创建下载器
	 * 
	 * @param taskSession 下载任务
	 * 
	 * @return 下载器
	 */
	public abstract IDownloader buildDownloader(TaskSession taskSession);
	
	/**
	 * <p>创建下载任务</p>
	 * <p>先检查是否需要转换协议，如果需要转换协议调用真实协议进行创建。</p>
	 */
	public TaskSession buildTaskSession() throws DownloadException {
		final Protocol convert = convert();
		if(convert != null) {
			return convert.buildTaskSession();
		}
		boolean ok = true;
		try {
			buildTaskEntity();
			return TaskSession.newInstance(this.taskEntity);
		} catch (DownloadException e) {
			ok = false;
			throw e;
		} catch (Exception e) {
			ok = false;
			throw new DownloadException("下载失败", e);
		} finally {
			clean(ok);
		}
	}

	/**
	 * <p>协议转换</p>
	 * <p>如果返回值不为空，则使用返回的协议进行下载。</p>
	 */
	protected Protocol convert() throws DownloadException {
		return null;
	}
	
	/**
	 * 创建下载任务
	 */
	protected void buildTaskEntity() throws DownloadException {
		this.taskEntity = new TaskEntity();
		prep();
		buildUrl();
		buildType();
		buildStatus();
		final String fileName = buildFileName();
		buildName(fileName);
		buildFile(fileName);
		buildFileType(fileName);
		buildSize();
		done();
		persistentTaskEntity();
	}
	
	/**
	 * 预处理
	 */
	protected void prep() throws DownloadException {
	}
	
	/**
	 * 设置URL
	 */
	protected void buildUrl() throws DownloadException {
		this.taskEntity.setUrl(this.url);
	}

	/**
	 * 设置下载类型
	 */
	protected void buildType() throws DownloadException {
		this.taskEntity.setType(this.type);
	}

	/**
	 * 设置任务状态
	 */
	protected void buildStatus() throws DownloadException {
		this.taskEntity.setStatus(Status.AWAIT);
	}

	/**
	 * 获取文件名称
	 */
	protected String buildFileName() throws DownloadException {
		String fileName = FileUtils.fileNameFromUrl(this.url);
		fileName = FileUtils.fileName(fileName);
		return fileName;
	}

	/**
	 * 设置任务名称
	 */
	protected void buildName(String fileName) throws DownloadException {
		String name;
		int index = fileName.lastIndexOf(".");
		if(index != -1) {
			name = fileName.substring(0, index);
		} else {
			name = fileName;
		}
		this.taskEntity.setName(name);
	}
	
	/**
	 * 设置文件、文件夹
	 */
	protected void buildFile(String fileName) throws DownloadException {
		final String filePath = DownloadConfig.getPath(fileName);
		final File file = new File(filePath);
		if(file.exists()) {
			throw new DownloadException("下载文件已经存在：" + file);
		}
		this.taskEntity.setFile(filePath);
	}
	
	/**
	 * 设置任务文件类型
	 */
	protected void buildFileType(String fileName) throws DownloadException {
		this.taskEntity.setFileType(FileUtils.fileType(fileName));
	}

	/**
	 * 设置任务大小
	 */
	protected void buildSize() throws DownloadException {
	}
	
	/**
	 * 完成处理
	 */
	protected void done() throws DownloadException {
	}
	
	/**
	 * 持久化任务
	 */
	protected void persistentTaskEntity() throws DownloadException {
		final TaskRepository repository = new TaskRepository();
		repository.save(this.taskEntity);
	}
	
	/**
	 * 清理数据
	 */
	protected void clean(boolean ok) {
		this.url = null;
		this.taskEntity = null;
		cleanMessage(ok);
	}
	
	/**
	 * 清理数据（子类）
	 * 
	 * @param ok 创建状态：true-成功；false-失败；
	 */
	protected abstract void cleanMessage(boolean ok);
	
}
