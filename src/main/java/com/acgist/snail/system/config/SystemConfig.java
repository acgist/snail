package com.acgist.snail.system.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.utils.NetUtils;

/**
 * 系统配置
 * TODO：UPNP映射端口时，如果已经映射需要修改端口
 */
public class SystemConfig extends PropertiesConfig {

	private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseConfig.class);
	
	private static final String SYSTEM_CONFIG = "/config/system.properties";
	
	/**
	 * 系统默认编码（file.encoding）
	 */
	public static final String DEFAULT_CHARSET = "utf-8";
	
	/**
	 * 无符号BYTE的大小
	 */
	public static final int UNSIGNED_BYTE_SIZE = 2 << 7;
	
	public static final String DIGIT = "0123456789";
	public static final String LETTER = "abcdefghijklmnopqrstuvwxyz";
	public static final String LETTER_UPPER = LETTER.toUpperCase();

	private static final SystemConfig INSTANCE = new SystemConfig();
	
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
	private Integer serverPort; // 服务端口
	private Integer peerPort; // Peer端口
	private Integer dhtPort; // DHT端口
	private Integer trackerSize; // 单个任务Tracker数量
	private Integer trackerMaxFailTimes; // Tracker失败次数
	private Integer peerSize; // 单个任务Peer数量
	private Integer peerOptimizeInterval; // 单个任务Peer优化周期（秒）
	private Integer peerDownloadSize; // 同时下载的Peer数量

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
		INSTANCE.serverPort = getInteger("acgist.server.port");
		INSTANCE.peerPort = getInteger("acgist.peer.port");
		INSTANCE.dhtPort = getInteger("acgist.dht.port");
		INSTANCE.trackerSize = getInteger("acgist.tracker.size");
		INSTANCE.trackerMaxFailTimes = getInteger("acgist.tracker.max.fail.times");
		INSTANCE.peerSize = getInteger("acgist.peer.size");
		INSTANCE.peerOptimizeInterval = getInteger("acgist.peer.optimize.interval");
		INSTANCE.peerDownloadSize = getInteger("acgist.peer.download.size");
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
		LOGGER.info("服务端口：{}", this.serverPort);
		LOGGER.info("Peer端口：{}", this.peerPort);
		LOGGER.info("DHT端口：{}", this.dhtPort);
		LOGGER.info("单个任务Tracker数量：{}", this.trackerSize);
		LOGGER.info("Tracker失败次数：{}", this.trackerMaxFailTimes);
		LOGGER.info("单个任务Peer数量：{}", this.peerSize);
		LOGGER.info("单个任务Peer优化周期（秒）：{}", this.peerOptimizeInterval);
		LOGGER.info("同时下载的Peer数量：{}", this.peerDownloadSize);
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
	public static final Integer getServerPort() {
		return INSTANCE.serverPort;
	}

	/**
	 * Peer端口
	 */
	public static final Integer getPeerPort() {
		return INSTANCE.peerPort;
	}
	
	/**
	 * Peer端口：short
	 */
	public static final Short getPeerPortShort() {
		return NetUtils.encodePort(getPeerPort());
	}

	/**
	 * DHT端口
	 */
	public static final Integer getDhtPort() {
		return INSTANCE.dhtPort;
	}

	/**
	 * 单个任务tracker数量
	 */
	public static final Integer getTrackerSize() {
		return INSTANCE.trackerSize;
	}
	
	/**
	 * Tracker失败次数
	 */
	public static final Integer getTrackerMaxFailTimes() {
		return INSTANCE.trackerMaxFailTimes;
	}

	/**
	 * 单个任务Peer数量
	 */
	public static final Integer getPeerSize() {
		return INSTANCE.peerSize;
	}

	/**
	 * 单个任务Peer优化周期
	 */
	public static final Integer getPeerOptimizeInterval() {
		return INSTANCE.peerOptimizeInterval;
	}

	/**
	 * 同时下载的Peer数量
	 */
	public static final Integer getPeerDownloadSize() {
		return INSTANCE.peerDownloadSize;
	}
	
	/**
	 * 获取名称和版本信息："名称 版本"
	 */
	public static final String getNameAndVersion() {
		return INSTANCE.nameEn + " " + INSTANCE.version;
	}

}
