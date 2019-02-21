package com.acgist.snail.module.config;

import com.acgist.snail.utils.PropertiesUtils;

/**
 * 数据库配置
 */
public class DatabaseConfig {

	private static final DatabaseConfig INSTANCE = new DatabaseConfig();
	
	private DatabaseConfig() {
	}

	static {
		PropertiesUtils propertiesUtils = PropertiesUtils.getInstance("/config/config.database.properties");
		INSTANCE.url = propertiesUtils.getString("database.h2.url");
		INSTANCE.driver = propertiesUtils.getString("database.h2.driver");
		INSTANCE.user = propertiesUtils.getString("database.h2.user");
		INSTANCE.password = propertiesUtils.getString("database.h2.password");
		INSTANCE.tableSQL = propertiesUtils.getString("database.h2.table.sql");
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
