package com.acgist.snail.system.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.utils.PropertiesUtils;

/**
 * 系统配置
 */
public class SystemConfig {

	private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseConfig.class);
	
	/**
	 * 系统默认编码（file.encoding）
	 */
	public static final String DEFAULT_CHARSET = "utf-8";

	private static final SystemConfig INSTANCE = new SystemConfig();
	
	private SystemConfig() {
	}

	static {
		LOGGER.info("初始化数据库配置");
		INSTANCE.init();
		INSTANCE.logger();
	}
	
	private String name; // 名称
	private String nameEn; // 英文名称
	private String version; // 版本
	private String author; // 作者
	private String source; // 源码
	private String support; // 支持
	private Integer serverPort; // 服务端口
	private String serverHost; // 服务地址
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
		PropertiesUtils propertiesUtils = PropertiesUtils.getInstance("/config/config.system.properties");
		INSTANCE.name = propertiesUtils.getString("acgist.system.name");
		INSTANCE.nameEn = propertiesUtils.getString("acgist.system.name.en");
		INSTANCE.version = propertiesUtils.getString("acgist.system.version");
		INSTANCE.author = propertiesUtils.getString("acgist.system.author");
		INSTANCE.source = propertiesUtils.getString("acgist.system.source");
		INSTANCE.support = propertiesUtils.getString("acgist.system.support");
		INSTANCE.serverPort = propertiesUtils.getInteger("acgist.server.port");
		INSTANCE.serverHost = propertiesUtils.getString("acgist.server.host");
		INSTANCE.peerPort = propertiesUtils.getInteger("acgist.peer.port");
		INSTANCE.dhtPort = propertiesUtils.getInteger("acgist.dht.port");
		INSTANCE.trackerSize = propertiesUtils.getInteger("acgist.tracker.size");
		INSTANCE.trackerMaxFailTimes = propertiesUtils.getInteger("acgist.tracker.max.fail.times");
		INSTANCE.peerSize = propertiesUtils.getInteger("acgist.peer.size");
		INSTANCE.peerOptimizeInterval = propertiesUtils.getInteger("acgist.peer.optimize.interval");
		INSTANCE.peerDownloadSize = propertiesUtils.getInteger("acgist.peer.download.size");
	}

	/**
	 * 日志
	 */
	private void logger() {
		LOGGER.info("-");
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
	 * 服务地址
	 */
	public static final String getServerHost() {
		return INSTANCE.serverHost;
	}

	/**
	 * Peer端口
	 */
	public static final Integer getPeerPort() {
		return INSTANCE.peerPort;
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
