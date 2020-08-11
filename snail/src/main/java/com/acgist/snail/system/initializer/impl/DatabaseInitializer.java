package com.acgist.snail.system.initializer.impl;

import java.io.IOException;
import java.io.InputStreamReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.repository.DatabaseManager;
import com.acgist.snail.system.config.DatabaseConfig;
import com.acgist.snail.system.initializer.Initializer;

/**
 * <p>初始化数据库</p>
 * 
 * @author acgist
 * @since 1.0.0
 */
public final class DatabaseInitializer extends Initializer {

	private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseInitializer.class);
	
	private final DatabaseManager databaseManager = DatabaseManager.getInstance();
	
	private DatabaseInitializer() {
	}
	
	public static final DatabaseInitializer newInstance() {
		return new DatabaseInitializer();
	}
	
	/**
	 * {@inheritDoc}
	 * 
	 * <p>如果数据库表没有创建：执行建表语句</p>
	 */
	@Override
	protected void init() {
		LOGGER.info("初始化数据库");
		if(this.databaseInit()) { // 已经创建
			LOGGER.debug("数据库已经初始化");
		} else { // 没有创建：执行创建语句
			this.buildTable();
		}
	}
	
	/**
	 * <p>查询数据库是否初始化</p>
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
		LOGGER.info("数据库建表");
		final String sql = this.buildTableSQL();
		this.databaseManager.update(sql);
	}

	/**
	 * <p>读取建表SQL</p>
	 * 
	 * @return 建表SQL
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
			LOGGER.error("读取建表SQL异常：{}", tableSQL, e);
		}
		return sql.toString();
	}
	
}
