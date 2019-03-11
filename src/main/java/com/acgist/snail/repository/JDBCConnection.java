package com.acgist.snail.repository;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.pojo.wrapper.ResultSetWrapper;
import com.acgist.snail.system.config.DatabaseConfig;
import com.acgist.snail.utils.CollectionUtils;
import com.acgist.snail.utils.EntityUtils;

/**
 * 数据库连接
 */
public class JDBCConnection {

	private static final Logger LOGGER = LoggerFactory.getLogger(JDBCConnection.class);
	
	/**
	 * 查询表是否存在
	 */
	public static final boolean hasTable(String table) {
		List<ResultSetWrapper> list = select("show tables");
		for (ResultSetWrapper resultSetWrapper : list) {
			if(table.equalsIgnoreCase(resultSetWrapper.getString("TABLE_NAME"))) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * 查询
	 */
	public static final List<ResultSetWrapper> select(String sql, Object ... parameters) {
		Connection connection = connection();
		ResultSet result = null;
		PreparedStatement statement = null;
		try {
			statement = connection.prepareStatement(sql);
			if(CollectionUtils.isNotEmpty(parameters)) {
				for (int index = 0; index < parameters.length; index++) {
					statement.setObject(index + 1, EntityUtils.pack(parameters[index]));
				}
			}
			result = statement.executeQuery();
			return wrapperResultSet(result);
		} catch (SQLException e) {
			LOGGER.error("执行SQL查询异常", e);
		} finally {
			close(result, statement, connection);
		}
		return null;
	}

	/**
	 * 更新
	 */
	public static final boolean update(String sql, Object ... parameters) {
		boolean ok = false;
		Connection connection = connection();
		PreparedStatement statement = null;
		try {
			statement = connection.prepareStatement(sql);
			if(CollectionUtils.isNotEmpty(parameters)) {
				for (int index = 0; index < parameters.length; index++) {
					statement.setObject(index + 1, EntityUtils.pack(parameters[index]));
				}
			}
			ok = statement.execute();
		} catch (SQLException e) {
			LOGGER.error("执行SQL更新异常", e);
		} finally {
			close(null, statement, connection);
		}
		return ok;
	}
	
	/**
	 * 结果集封装
	 */
	private static final List<ResultSetWrapper> wrapperResultSet(ResultSet result) throws SQLException {
		String[] columns = columnNames(result);
		List<ResultSetWrapper> list = new ArrayList<>();
		while(result.next()) {
			ResultSetWrapper wrapper = new ResultSetWrapper();
			for (String column : columns) {
				wrapper.put(column, result.getObject(column));
			}
			list.add(wrapper);
		}
		return list;
	}
	
	/**
	 * 获取列名
	 */
	private static final String[] columnNames(ResultSet result) throws SQLException {
		final ResultSetMetaData meta = result.getMetaData();
		final int count = meta.getColumnCount();
		final String[] columns = new String[count];
		for (int index = 0; index < count; index++) {
			columns[index] = meta.getColumnName(index + 1);
		}
		return columns;
	}

	private static final Connection connection() {
		Connection connection = null;
		try {
			Class.forName(DatabaseConfig.getDriver());
			connection = DriverManager.getConnection(DatabaseConfig.getUrl(), DatabaseConfig.getUser(), DatabaseConfig.getPassword());
		} catch (ClassNotFoundException | SQLException e) {
			LOGGER.error("打开JDBC连接异常", e);
		}
		return connection;
	}
	
	private static final void close(ResultSet result, PreparedStatement statement, Connection connection) {
		if(result != null) {
			try {
				result.close();
			} catch (SQLException e) {
				LOGGER.error("JDBC结果集关闭异常", e);
			}
		}
		if(statement != null) {
			try {
				statement.close();
			} catch (SQLException e) {
				LOGGER.error("JDBC语句执行器关闭异常", e);
			}
		}
		if(connection != null) {
			try {
				connection.close();
			} catch (SQLException e) {
				LOGGER.error("JDBC链接关闭异常", e);
			}
		}
	}
	
}
