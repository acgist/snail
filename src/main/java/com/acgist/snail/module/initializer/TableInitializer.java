package com.acgist.snail.module.initializer;

import java.io.IOException;
import java.io.InputStreamReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.module.config.DatabaseConfig;
import com.acgist.snail.pojo.entity.ConfigEntity;
import com.acgist.snail.repository.JDBCConnection;

/**
 * 数据库建表
 */
public class TableInitializer extends Initializer {

	private static final Logger LOGGER = LoggerFactory.getLogger(TableInitializer.class);
	
	public void init() {
		if(exist()) {
			return;
		}
		buildTable();
	}
	
	/**
	 * 判断表是否存在
	 */
	private boolean exist() {
		return JDBCConnection.hasTable(ConfigEntity.TABLE_NAME);
	}

	/**
	 * 初始化数据库表
	 */
	private void buildTable() {
		LOGGER.info("初始化数据库表");
		String sql = buildTableSQL();
		JDBCConnection.update(sql);
	}

	/**
	 * 读取初始化SQL
	 */
	private String buildTableSQL() {
		StringBuilder sql = new StringBuilder();
		try(InputStreamReader reader = new InputStreamReader(TableInitializer.class.getResourceAsStream(DatabaseConfig.getTableSQL()))) {
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
