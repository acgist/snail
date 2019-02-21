package com.acgist.snail.module.config;

import com.acgist.snail.utils.PropertiesUtils;

/**
 * 系统配置
 */
public class SystemConfig {

	public static final String DEFAULT_CHARSET = "utf-8";

	private static final SystemConfig INSTANCE = new SystemConfig();
	
	private SystemConfig() {
	}

	static {
		PropertiesUtils propertiesUtils = PropertiesUtils.getInstance("/config/config.system.properties");
		INSTANCE.name = propertiesUtils.getString("acgist.system.name");
		INSTANCE.author = propertiesUtils.getString("acgist.system.author");
		INSTANCE.source = propertiesUtils.getString("acgist.system.source");
		INSTANCE.support = propertiesUtils.getString("acgist.system.support");
		INSTANCE.serverPort = propertiesUtils.getInteger("acgist.server.port");
		INSTANCE.serverHost = propertiesUtils.getString("acgist.server.host");
	}
	
	private String name;
	private String author;
	private String source;
	private String support;
	private Integer serverPort;
	private String serverHost;
	
	public static final String getName() {
		return INSTANCE.name;
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
