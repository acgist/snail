package com.acgist.snail.module.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.utils.PropertiesUtils;

/**
 * 数据库配置
 */
public class DatabaseConfig {

	private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseConfig.class);
	
	private static final DatabaseConfig INSTANCE = new DatabaseConfig();
	
	private DatabaseConfig() {
	}

	static {
		LOGGER.info("初始化数据库配置");
		PropertiesUtils propertiesUtils = PropertiesUtils.getInstance("/config/config.database.properties");
		INSTANCE.url = propertiesUtils.getString("acgist.database.h2.url");
		INSTANCE.driver = propertiesUtils.getString("acgist.database.h2.driver");
		INSTANCE.user = propertiesUtils.getString("acgist.database.h2.user");
		INSTANCE.password = propertiesUtils.getString("acgist.database.h2.password");
		INSTANCE.tableSQL = propertiesUtils.getString("acgist.database.h2.table.sql");
		LOGGER.info("数据库地址：{}", INSTANCE.url);
		LOGGER.info("数据库驱动：{}", INSTANCE.driver);
		LOGGER.info("数据库用户：{}", INSTANCE.user);
		LOGGER.info("数据库密码：{}", INSTANCE.password);
		LOGGER.info("数据库建表语句：{}", INSTANCE.tableSQL);
	}
	
	private String url;
	private String driver;
	private String user;
	private String password;
	private String tableSQL;

	public static final String getUrl() {
		return INSTANCE.url;
	}
	
	public static final String getDriver() {
		return INSTANCE.driver;
	}
	
	public static final String getUser() {
		return INSTANCE.user;
	}
	
	public static final String getPassword() {
		return INSTANCE.password;
	}
	
	public static final String getTableSQL() {
		return INSTANCE.tableSQL;
	}

}
