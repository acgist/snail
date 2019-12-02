package com.acgist.snail.protocol;

import java.io.File;

import com.acgist.snail.downloader.IDownloader;
import com.acgist.snail.pojo.ITaskSession;
import com.acgist.snail.pojo.ITaskSession.Status;
import com.acgist.snail.pojo.entity.TaskEntity;
import com.acgist.snail.pojo.session.TaskSession;
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
	 * <p>磁力链接：标准</p>
	 */
	private static final String MAGNET_BASIC = "magnet:\\?.+";
	/**
	 * <p>磁力链接：32位HASH</p>
	 */
	private static final String MAGNET_HASH_32 = "[a-zA-Z0-9]{32}";
	/**
	 * <p>磁力链接：40位HASH</p>
	 */
	private static final String MAGNET_HASH_40 = "[a-zA-Z0-9]{40}";
	
	/**
	 * <p>协议类型</p>
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
			new String[] {MAGNET_BASIC, MAGNET_HASH_32, MAGNET_HASH_40},
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
		/** BT：BitTorrent */
		TORRENT(
			new String[] {".+\\.torrent"},
			new String[] {},
			new String[] {".torrent"},
			"",
			".torrent"
		);
		
		/**
		 * <p>正则表达式</p>
		 */
		private final String[] regexs;
		/**
		 * <p>前缀</p>
		 */
		private final String[] prefix;
		/**
		 * <p>后缀</p>
		 */
		private final String[] suffix;
		/**
		 * <p>默认前缀</p>
		 */
		private final String defaultPrefix;
		/**
		 * <p>默认后缀</p>
		 */
		private final String defaultSuffix;
		
		private Type(String[] regexs, String[] prefix, String[] suffix, String defaultPrefix, String defaultSuffix) {
			this.regexs = regexs;
			this.prefix = prefix;
			this.suffix = suffix;
			this.defaultPrefix = defaultPrefix;
			this.defaultSuffix = defaultSuffix;
		}

		/**
		 * @return 正则表达式
		 */
		public String[] regexs() {
			return this.regexs;
		}

		/**
		 * @return 前缀
		 */
		public String[] prefix() {
			return this.prefix;
		}
		
		/**
		 * <p>获取链接的前缀</p>
		 * 
		 * @param url 链接
		 * 
		 * @return 前缀
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
		 * @return 后缀
		 */
		public String[] suffix() {
			return this.suffix;
		}
		
		/**
		 * <p>获取链接的后缀</p>
		 * 
		 * @param url 链接
		 * 
		 * @return 后缀
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
		 * <p>验证链接是否属于该协议</p>
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
		 * <p>将HASH转为磁力链接（完整链接）</p>
		 */
		public static final String buildMagnet(String hash) {
			if(verifyMagnet(hash)) {
				return hash;
			}
			return Type.MAGNET.defaultPrefix + hash.toLowerCase();
		}
		
		/**
		 * <p>验证磁力链接（完整链接）</p>
		 */
		public static final boolean verifyMagnet(String url) {
			return StringUtils.regex(url, MAGNET_BASIC, true);
		}
		
		/**
		 * <p>验证32位磁力链接HASH</p>
		 */
		public static final boolean verifyMagnetHash32(String url) {
			return StringUtils.regex(url, MAGNET_HASH_32, true);
		}
		
		/**
		 * <p>验证40位磁力链接HASH</p>
		 */
		public static final boolean verifyMagnetHash40(String url) {
			return StringUtils.regex(url, MAGNET_HASH_40, true);
		}
		
	}
	
	/**
	 * <p>下载任务类型</p>
	 */
	protected final Type type;
	/**
	 * <p>下载地址</p>
	 */
	protected String url;
	/**
	 * <p>下载任务</p>
	 */
	protected TaskEntity taskEntity;
	
	public Protocol(Type type) {
		this.type = type;
	}

	/**
	 * <p>初始化</p>
	 */
	public Protocol init(String url) {
		this.url = url.trim();
		return this;
	}
	
	/**
	 * <p>获取协议类型</p>
	 * 
	 * @return 协议类型
	 */
	public Type type() {
		return this.type;
	}
	
	/**
	 * <p>获取协议名称</p>
	 * 
	 * @return 协议名称
	 */
	public abstract String name();
	
	/**
	 * <p>验证是否支持协议</p>
	 */
	public boolean verify() {
		if(this.type == null) {
			return false;
		}
		return this.type.verify(this.url);
	}
	
	/**
	 * <p>是否可用</p>
	 * 
	 * @return 是否可用
	 */
	public abstract boolean available();
	
	/**
	 * <p>创建下载器</p>
	 * 
	 * @param taskSession 下载任务
	 * 
	 * @return 下载器
	 */
	public abstract IDownloader buildDownloader(ITaskSession taskSession);
	
	/**
	 * <p>创建下载任务</p>
	 * <p>存在协议转换：使用转换后的协议下载</p>
	 * <p>不存在协议转换：使用当前协议下载</p>
	 */
	public ITaskSession buildTaskSession() throws DownloadException {
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
	 * <p>如果存在协议转换返回：转换后协议</p>
	 * <p>如果不存在协议转换返回：null</p>
	 */
	protected Protocol convert() throws DownloadException {
		return null;
	}
	
	/**
	 * <p>创建下载任务</p>
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
	 * <p>预处理</p>
	 */
	protected void prep() throws DownloadException {
	}
	
	/**
	 * <p>设置URL</p>
	 */
	protected void buildUrl() throws DownloadException {
		this.taskEntity.setUrl(this.url);
	}

	/**
	 * <p>设置下载类型</p>
	 */
	protected void buildType() throws DownloadException {
		this.taskEntity.setType(this.type);
	}

	/**
	 * <p>设置任务状态</p>
	 */
	protected void buildStatus() throws DownloadException {
		this.taskEntity.setStatus(Status.AWAIT);
	}

	/**
	 * <p>获取文件名称</p>
	 */
	protected String buildFileName() throws DownloadException {
		String fileName = FileUtils.fileNameFromUrl(this.url);
		fileName = FileUtils.fileNameFormat(fileName);
		return fileName;
	}

	/**
	 * <p>设置任务名称</p>
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
	 * <p>设置下载文件、文件夹</p>
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
	 * <p>设置任务文件类型</p>
	 */
	protected void buildFileType(String fileName) throws DownloadException {
		this.taskEntity.setFileType(FileUtils.fileType(fileName));
	}

	/**
	 * <p>设置任务大小</p>
	 */
	protected void buildSize() throws DownloadException {
	}
	
	/**
	 * <p>完成处理</p>
	 */
	protected void done() throws DownloadException {
	}
	
	/**
	 * <p>持久化任务</p>
	 */
	protected void persistentTaskEntity() throws DownloadException {
		final TaskRepository repository = new TaskRepository();
		repository.save(this.taskEntity);
	}
	
	/**
	 * <p>清理数据</p>
	 */
	protected void clean(boolean ok) {
		this.url = null;
		this.taskEntity = null;
		cleanMessage(ok);
	}
	
	/**
	 * <p>清理数据（子类）</p>
	 * 
	 * @param ok 创建状态：true-成功；false-失败；
	 */
	protected abstract void cleanMessage(boolean ok);
	
}
