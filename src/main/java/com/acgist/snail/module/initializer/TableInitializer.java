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
public class TableInitializer {

	private static final Logger LOGGER = LoggerFactory.getLogger(TableInitializer.class);
	
	public static final void init() {
		if(exist()) {
			return;
		}
		buildTable();
	}
	
	private static final boolean exist() {
		return JDBCConnection.hasTable(ConfigEntity.TABLE_NAME);
	}

	private static final void buildTable() {
		LOGGER.info("初始化数据库");
		String sql = buildTableSQL();
		JDBCConnection.update(sql);
	}

	private static final String buildTableSQL() {
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
