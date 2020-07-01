package com.acgist.snail.system.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>数据库配置</p>
 * 
 * @author acgist
 * @since 1.0.0
 */
public final class DatabaseConfig extends PropertiesConfig {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseConfig.class);
	
	private static final DatabaseConfig INSTANCE = new DatabaseConfig();
	
	/**
	 * <p>配置文件：{@value}</p>
	 */
	private static final String DATABASE_CONFIG = "/config/database.properties";
	
	private DatabaseConfig() {
		super(DATABASE_CONFIG);
	}

	static {
		LOGGER.info("初始化数据库配置");
		INSTANCE.init();
		INSTANCE.logger();
	}
	
	/**
	 * <p>数据库地址</p>
	 */
	private String url;
	/**
	 * <p>数据库驱动</p>
	 */
	private String driver;
	/**
	 * <p>数据库用户</p>
	 */
	private String user;
	/**
	 * <p>数据库密码</p>
	 */
	private String password;
	/**
	 * <p>数据库建表文件</p>
	 */
	private String tableSQL;
	
	public static final DatabaseConfig getInstance() {
		return INSTANCE;
	}

	/**
	 * <p>初始化</p>
	 */
	private void init() {
		INSTANCE.url = getString("acgist.database.h2.url");
		INSTANCE.driver = getString("acgist.database.h2.driver");
		INSTANCE.user = getString("acgist.database.h2.user");
		INSTANCE.password = getString("acgist.database.h2.password");
		INSTANCE.tableSQL = getString("acgist.database.h2.table.sql");
	}
	
	/**
	 * <p>日志</p>
	 */
	private void logger() {
		LOGGER.info("数据库地址：{}", this.url);
		LOGGER.info("数据库驱动：{}", this.driver);
		LOGGER.info("数据库用户：{}", this.user);
		LOGGER.info("数据库密码：{}", this.password);
		LOGGER.info("数据库建表文件：{}", this.tableSQL);
	}
	
	/**
	 * <p>获取数据库地址</p>
	 * 
	 * @return 数据库地址
	 */
	public static final String getUrl() {
		return INSTANCE.url;
	}
	
	/**
	 * <p>获取数据库驱动</p>
	 * 
	 * @return 数据库驱动
	 */
	public static final String getDriver() {
		return INSTANCE.driver;
	}

	/**
	 * <p>获取数据库用户</p>
	 * 
	 * @return 数据库用户
	 */
	public static final String getUser() {
		return INSTANCE.user;
	}

	/**
	 * <p>获取数据库密码</p>
	 * 
	 * @return 数据库密码
	 */
	public static final String getPassword() {
		return INSTANCE.password;
	}
	
	/**
	 * <p>获取数据库建表文件</p>
	 * 
	 * @return 数据库建表文件
	 */
	public static final String getTableSQL() {
		return INSTANCE.tableSQL;
	}

}
