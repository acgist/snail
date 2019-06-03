package com.acgist.snail.system.initializer.impl;

import java.io.IOException;
import java.io.InputStreamReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.pojo.entity.ConfigEntity;
import com.acgist.snail.system.config.DatabaseConfig;
import com.acgist.snail.system.initializer.Initializer;
import com.acgist.snail.system.manager.DatabaseManager;

/**
 * 初始化：数据库建表
 */
public class DatabaseInitializer extends Initializer {

	private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseInitializer.class);
	
	private DatabaseManager jdbcConnection = DatabaseManager.getInstance();
	
	private DatabaseInitializer() {
	}
	
	public static final DatabaseInitializer newInstance() {
		return new DatabaseInitializer();
	}
	
	@Override
	protected void init() {
		if(exist()) {
			return;
		}
		buildTable();
	}
	
	/**
	 * 判断表是否存在
	 */
	private boolean exist() {
		return jdbcConnection.haveTable(ConfigEntity.TABLE_NAME);
	}

	/**
	 * 初始化数据库表
	 */
	private void buildTable() {
		LOGGER.info("初始化数据库表");
		String sql = buildTableSQL();
		jdbcConnection.update(sql);
	}

	/**
	 * 读取初始化SQL
	 */
	private String buildTableSQL() {
		final StringBuilder sql = new StringBuilder();
		final String sqlFilePath = DatabaseConfig.getTableSQL();
		try(InputStreamReader reader = new InputStreamReader(DatabaseInitializer.class.getResourceAsStream(sqlFilePath))) {
			int count = 0;
			char[] chars = new char[1024];
			while((count = reader.read(chars)) != -1) {
				sql.append(new String(chars, 0, count));
			}
		} catch (IOException e) {
			LOGGER.error("建表SQL读取异常：{}", sqlFilePath, e);
		}
		return sql.toString();
	}
	
}
