package com.acgist.snail.system.config;

import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.utils.NetUtils;

/**
 * <p>系统配置</p>
 * 
 * @author acgist
 * @since 1.0.0
 */
public class SystemConfig extends PropertiesConfig {

	private static final Logger LOGGER = LoggerFactory.getLogger(SystemConfig.class);
	
	private static final String SYSTEM_CONFIG = "/config/system.properties";

	/**
	 * 消息缓冲大小
	 */
	public static final int BUFFER_SIZE = 10 * 1024;
	/**
	 * <p>最大的网络包大小。</p>
	 * <p>所有的需要创建ByteBuffer的长度由外部Peer设置时需要验证长度，防止恶意攻击导致内存泄露。</p>
	 */
	public static final int MAX_NET_BUFFER_SIZE = 10 * 1024 * 1024;
	/**
	 * 编码：GBK
	 */
	public static final String CHARSET_GBK = "GBK";
	/**
	 * 编码：UTF-8
	 */
	public static final String CHARSET_UTF8 = "UTF-8";
	/**
	 * 编码：ASCII
	 */
	public static final String CHARSET_ASCII = "ASCII";
	/**
	 * 编码：ISO-8859-1
	 */
	public static final String CHARSET_ISO_8859_1 = "ISO-8859-1";
	/**
	 * 系统默认编码（file.encoding）
	 */
	public static final String DEFAULT_CHARSET = CHARSET_UTF8;
	/**
	 * 无符号BYTE的大小
	 */
	public static final int UNSIGNED_BYTE_SIZE = 2 << 7;
	/**
	 * 任务列表刷新时间
	 */
	public static final Duration TASK_REFRESH_INTERVAL = Duration.ofSeconds(4);
	
	public static final String DIGIT = "0123456789";
	public static final String LETTER = "abcdefghijklmnopqrstuvwxyz";
	public static final String LETTER_UPPER = LETTER.toUpperCase();

	private static final SystemConfig INSTANCE = new SystemConfig();
	
	/**
	 * <p>用户工作目录</p>
	 * <p>注意顺序：优先初始化，不能使用类变量，本类初始化时会使用。</p>
	 */
	private static final String USER_DIR = System.getProperty("user.dir");
	
	private SystemConfig() {
		super(SYSTEM_CONFIG);
	}

	static {
		LOGGER.info("初始化数据库配置");
		INSTANCE.init();
		INSTANCE.logger();
	}

	public static final SystemConfig getInstance() {
		return INSTANCE;
	}
	
	private String name; // 名称
	private String nameEn; // 英文名称
	private String version; // 版本
	
	private String author; // 作者
	private String source; // 源码
	private String support; // 支持
	
	private Integer servicePort; // 服务端口（本地服务：启动检测）
	private Integer torrentPort; // 服务端口（本地端口：Peer、DHT、UTP）
	private Integer torrentPortExt; // 服务端口（外网映射：Peer、DHT、UTP）
	
	private Integer peerSize; // 单个任务Peer数量
	private Integer trackerSize; // 单个任务Tracker数量
	private Integer pieceRepeatSize; // 任务即将完成时可以重复选择下载的剩下Piece数量
	
	private Integer dhtInterval; // DHT执行周期（秒）
	private Integer pexInterval; // PEX执行周期（秒）
	private Integer trackerInterval; // Tracker执行周期（秒）
	private Integer peerOptimizeInterval; // 单个任务Peer优化周期（秒）
	
	/**
	 * 初始化
	 */
	private void init() {
		INSTANCE.name = getString("acgist.system.name");
		INSTANCE.nameEn = getString("acgist.system.name.en");
		INSTANCE.version = getString("acgist.system.version");
		INSTANCE.author = getString("acgist.system.author");
		INSTANCE.source = getString("acgist.system.source");
		INSTANCE.support = getString("acgist.system.support");
		INSTANCE.servicePort = getInteger("acgist.service.port");
		INSTANCE.torrentPort = getInteger("acgist.torrent.port");
		INSTANCE.peerSize = getInteger("acgist.peer.size");
		INSTANCE.trackerSize = getInteger("acgist.tracker.size");
		INSTANCE.pieceRepeatSize = getInteger("acgist.piece.repeat.size");
		INSTANCE.dhtInterval = getInteger("acgist.dht.interval");
		INSTANCE.pexInterval = getInteger("acgist.pex.interval");
		INSTANCE.trackerInterval = getInteger("acgist.tracker.interval");
		INSTANCE.peerOptimizeInterval = getInteger("acgist.peer.optimize.interval");
	}

	/**
	 * 日志
	 */
	private void logger() {
		LOGGER.info("名称：{}", this.name);
		LOGGER.info("英文名称：{}", this.nameEn);
		LOGGER.info("版本：{}", this.version);
		LOGGER.info("作者：{}", this.author);
		LOGGER.info("源码：{}", this.source);
		LOGGER.info("支持：{}", this.support);
		LOGGER.info("系统端口：{}", this.servicePort);
		LOGGER.info("服务端口（Peer、DHT、UTP）：{}", this.torrentPort);
		LOGGER.info("单个任务Peer数量：{}", this.peerSize);
		LOGGER.info("单个任务Tracker数量：{}", this.trackerSize);
		LOGGER.info("任务即将完成时可以重复选择下载的剩下Piece数量：{}", this.pieceRepeatSize);
		LOGGER.info("DHT执行周期（秒）：{}", this.dhtInterval);
		LOGGER.info("PEX执行周期（秒）：{}", this.pexInterval);
		LOGGER.info("Tracker执行周期（秒）：{}", this.trackerInterval);
		LOGGER.info("单个任务Peer优化周期（秒）：{}", this.peerOptimizeInterval);
		LOGGER.info("用户工作目录：{}", SystemConfig.USER_DIR);
	}
	
	/**
	 * 名称
	 */
	public static final String getName() {
		return INSTANCE.name;
	}

	/**
	 * 英文名称
	 */
	public static final String getNameEn() {
		return INSTANCE.nameEn;
	}

	/**
	 * 版本
	 */
	public static final String getVersion() {
		return INSTANCE.version;
	}

	/**
	 * 作者
	 */
	public static final String getAuthor() {
		return INSTANCE.author;
	}

	/**
	 * 源码
	 */
	public static final String getSource() {
		return INSTANCE.source;
	}

	/**
	 * 支持
	 */
	public static final String getSupport() {
		return INSTANCE.support;
	}

	/**
	 * 服务端口
	 */
	public static final Integer getServicePort() {
		return INSTANCE.servicePort;
	}

	/**
	 * <p>服务端口（Peer、DHT、UTP）</p>
	 * <p>本机注册使用</p>
	 */
	public static final Integer getTorrentPort() {
		return INSTANCE.torrentPort;
	}
	
	/**
	 * 设置服务端口（外网：Peer、DHT、UTP），映射时如果端口已经被占用时重新设置的外网端口号。
	 */
	public static final void setTorrentPortExt(Integer torrentPortExt) {
		LOGGER.info("服务端口（外网：Peer、DHT、UTP）：{}", torrentPortExt);
		INSTANCE.torrentPortExt = torrentPortExt;
	}
	
	/**
	 * <p>服务端口（外网：Peer、DHT、UTP）</p>
	 * <p>外网使用，外网的Peer连接此端口。</p>
	 * <p>如果不存在返回{@linkplain #getTorrentPort() 本机端口}。</p>
	 */
	public static final Integer getTorrentPortExt() {
		if(INSTANCE.torrentPortExt == null) {
			return getTorrentPort();
		}
		return INSTANCE.torrentPortExt;
	}
	
	/**
	 * 服务端口（外网：Peer、DHT、UTP）：short
	 */
	public static final Short getTorrentPortExtShort() {
		return NetUtils.encodePort(getTorrentPortExt());
	}
	
	/**
	 * 单个任务Peer数量
	 */
	public static final Integer getPeerSize() {
		return INSTANCE.peerSize;
	}
	
	/**
	 * 单个任务tracker数量
	 */
	public static final Integer getTrackerSize() {
		return INSTANCE.trackerSize;
	}

	/**
	 * 任务即将完成时可以重复选择下载的剩下Piece数量
	 */
	public static final Integer getPieceRepeatSize() {
		return INSTANCE.pieceRepeatSize;
	}

	/**
	 * DHT执行周期（秒）
	 */
	public static final Integer getDhtInterval() {
		return INSTANCE.dhtInterval;
	}
	
	/**
	 * PEX执行周期（秒）
	 */
	public static final Integer getPexInterval() {
		return INSTANCE.pexInterval;
	}
	
	/**
	 * Tracker执行周期（秒）
	 */
	public static final Integer getTrackerInterval() {
		return INSTANCE.trackerInterval;
	}
	
	/**
	 * 单个任务Peer优化周期
	 */
	public static final Integer getPeerOptimizeInterval() {
		return INSTANCE.peerOptimizeInterval;
	}

	/**
	 * 用户工作目录
	 */
	public static final String userDir() {
		return SystemConfig.USER_DIR;
	}
	
	/**
	 * 在用户工作目录中获取文件路径
	 * 
	 * @param path 文件相对路径：以/开头
	 */
	public static final String userDir(String path) {
		return SystemConfig.USER_DIR + path;
	}
	
	/**
	 * 获取名称和版本信息："名称 版本"
	 */
	public static final String getNameEnAndVersion() {
		return INSTANCE.nameEn + " " + INSTANCE.version;
	}

}
