package com.acgist.snail.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>数据库配置</p>
 * 
 * @author acgist
 */
public final class DatabaseConfig extends PropertiesConfig {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseConfig.class);
	
	/**
	 * <p>单例对象</p>
	 */
	private static final DatabaseConfig INSTANCE = new DatabaseConfig();
	
	/**
	 * <p>获取单例对象</p>
	 * 
	 * @return 单例对象
	 */
	public static final DatabaseConfig getInstance() {
		return INSTANCE;
	}
	
	/**
	 * <p>配置文件：{@value}</p>
	 */
	private static final String DATABASE_CONFIG = "/config/database.properties";
	
	static {
		LOGGER.info("初始化数据库配置：{}", DATABASE_CONFIG);
		INSTANCE.init();
		INSTANCE.logger();
		INSTANCE.release();
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

	/**
	 * <p>禁止创建实例</p>
	 */
	private DatabaseConfig() {
		super(DATABASE_CONFIG);
	}
	
	/**
	 * <p>初始化配置</p>
	 */
	private void init() {
		INSTANCE.url = this.getString("acgist.database.h2.url");
		INSTANCE.driver = this.getString("acgist.database.h2.driver");
		INSTANCE.user = this.getString("acgist.database.h2.user");
		INSTANCE.password = this.getString("acgist.database.h2.password");
		INSTANCE.tableSQL = this.getString("acgist.database.h2.table.sql");
	}
	
	/**
	 * <p>记录日志</p>
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
