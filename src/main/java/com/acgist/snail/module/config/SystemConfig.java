package com.acgist.snail.module.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.utils.PropertiesUtils;

/**
 * 系统配置
 */
public class SystemConfig {

	private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseConfig.class);
	
	public static final String DEFAULT_CHARSET = "utf-8";

	private static final SystemConfig INSTANCE = new SystemConfig();
	
	private SystemConfig() {
	}

	static {
		LOGGER.info("初始化数据库配置");
		PropertiesUtils propertiesUtils = PropertiesUtils.getInstance("/config/config.system.properties");
		INSTANCE.name = propertiesUtils.getString("acgist.system.name");
		INSTANCE.nameEn = propertiesUtils.getString("acgist.system.name.en");
		INSTANCE.version = propertiesUtils.getString("acgist.system.version");
		INSTANCE.author = propertiesUtils.getString("acgist.system.author");
		INSTANCE.source = propertiesUtils.getString("acgist.system.source");
		INSTANCE.support = propertiesUtils.getString("acgist.system.support");
		INSTANCE.serverPort = propertiesUtils.getInteger("acgist.server.port");
		INSTANCE.serverHost = propertiesUtils.getString("acgist.server.host");
	}
	
	private String name; // 名称
	private String nameEn; // 英文名称
	private String version; // 版本
	private String author; // 作者
	private String source; // 源码
	private String support; // 支持
	private Integer serverPort; // 服务端口
	private String serverHost; // 服务地址
	
	public static final String getName() {
		return INSTANCE.name;
	}
	
	public static final String getNameEn() {
		return INSTANCE.nameEn;
	}
	
	public static final String getVersion() {
		return INSTANCE.version;
	}

	public static final String getAuthor() {
		return INSTANCE.author;
	}

	public static final String getSource() {
		return INSTANCE.source;
	}

	public static final String getSupport() {
		return INSTANCE.support;
	}

	public static final Integer getServerPort() {
		return INSTANCE.serverPort;
	}

	public static final String getServerHost() {
		return INSTANCE.serverHost;
	}

}
