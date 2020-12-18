package com.acgist.snail.context.initializer.impl;

import java.io.IOException;
import java.io.InputStreamReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.config.DatabaseConfig;
import com.acgist.snail.context.initializer.Initializer;
import com.acgist.snail.repository.DatabaseManager;

/**
 * <p>初始化数据库</p>
 * 
 * @author acgist
 */
public final class DatabaseInitializer extends Initializer {

	private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseInitializer.class);
	
	/**
	 * <p>数据库管理器</p>
	 */
	private final DatabaseManager databaseManager = DatabaseManager.getInstance();
	
	private DatabaseInitializer() {
	}
	
	/**
	 * <p>创建初始化数据库</p>
	 * 
	 * @return DatabaseInitializer
	 */
	public static final DatabaseInitializer newInstance() {
		return new DatabaseInitializer();
	}
	
	/**
	 * {@inheritDoc}
	 * 
	 * <p>数据库没有初始化：执行建表语句</p>
	 */
	@Override
	protected void init() {
		LOGGER.info("初始化数据库");
		if(this.databaseInit()) {
			// 已经创建
			LOGGER.debug("数据库已经初始化");
		} else {
			// 没有创建：执行创建语句
			this.buildTable();
		}
	}
	
	/**
	 * <p>判断数据库是否初始化</p>
	 * 
	 * @return 是否初始化
	 */
	private boolean databaseInit() {
		return this.databaseManager.databaseInit();
	}

	/**
	 * <p>执行建表语句</p>
	 */
	private void buildTable() {
		final String sql = this.buildTableSQL();
		LOGGER.info("建表SQL：{}", sql);
		this.databaseManager.update(sql);
	}

	/**
	 * <p>读取建表语句</p>
	 * 
	 * @return 建表语句
	 */
	private String buildTableSQL() {
		final StringBuilder sql = new StringBuilder();
		final String tableSQL = DatabaseConfig.getTableSQL();
		try(final var reader = new InputStreamReader(DatabaseInitializer.class.getResourceAsStream(tableSQL))) {
			int count = 0;
			final char[] chars = new char[1024];
			while((count = reader.read(chars)) != -1) {
				sql.append(new String(chars, 0, count));
			}
		} catch (IOException e) {
			LOGGER.error("读取建表语句异常：{}", tableSQL, e);
		}
		return sql.toString();
	}
	
}
