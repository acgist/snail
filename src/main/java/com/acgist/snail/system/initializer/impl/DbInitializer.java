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
public class DbInitializer extends Initializer {

	private static final Logger LOGGER = LoggerFactory.getLogger(DbInitializer.class);
	
	private DatabaseManager jdbcConnection = DatabaseManager.getInstance();
	
	private DbInitializer() {
	}
	
	public static final DbInitializer newInstance() {
		return new DbInitializer();
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
		return jdbcConnection.hasTable(ConfigEntity.TABLE_NAME);
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
		StringBuilder sql = new StringBuilder();
		try(InputStreamReader reader = new InputStreamReader(DbInitializer.class.getResourceAsStream(DatabaseConfig.getTableSQL()))) {
			int count = 0;
			char[] chars = new char[1024];
			while((count = reader.read(chars)) != -1) {
				sql.append(new String(chars, 0, count));
			}
		} catch (IOException e) {
			LOGGER.error("读取建表SQL异常", e);
		}
		return sql.toString();
	}
	
}
