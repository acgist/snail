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
import com.acgist.snail.system.exception.RepositoryException;
import com.acgist.snail.utils.ArrayUtils;
import com.acgist.snail.utils.BeanUtils;

/**
 * <p>数据库管理器</p>
 * 
 * @author acgist
 * @since 1.0.0
 */
public class DatabaseManager {

	private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseManager.class);
	
	private static final DatabaseManager INSTANCE = new DatabaseManager();
	
	private DatabaseManager() {
	}
	
	public static final DatabaseManager getInstance() {
		return INSTANCE;
	}
	
	/**
	 * 连接
	 */
	private Connection connection;

	/**
	 * 查询表是否存在
	 */
	public boolean hasTable(String table) {
		final List<ResultSetWrapper> tables = select("show tables");
		for (ResultSetWrapper wrapper : tables) {
			if(table.equalsIgnoreCase(wrapper.getString("TABLE_NAME"))) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * 查询
	 */
	public List<ResultSetWrapper> select(String sql, Object ... parameters) {
		ResultSet result = null;
		PreparedStatement statement = null;
		try {
			final Connection connection = connection();
			statement = connection.prepareStatement(sql);
			if(ArrayUtils.isNotEmpty(parameters)) {
				for (int index = 0; index < parameters.length; index++) {
					statement.setObject(index + 1, BeanUtils.pack(parameters[index]));
				}
			}
			result = statement.executeQuery();
			return wrapperResultSet(result);
		} catch (SQLException e) {
			LOGGER.error("执行SQL查询异常：{}", sql, e);
			closeConnection();
			throw new RepositoryException(e);
		} finally {
			close(result, statement);
		}
	}

	/**
	 * 更新
	 */
	public boolean update(String sql, Object ... parameters) {
		boolean ok = false;
		PreparedStatement statement = null;
		try {
			final Connection connection = connection();
			statement = connection.prepareStatement(sql);
			if(ArrayUtils.isNotEmpty(parameters)) {
				for (int index = 0; index < parameters.length; index++) {
					statement.setObject(index + 1, BeanUtils.pack(parameters[index]));
				}
			}
			ok = statement.execute();
		} catch (SQLException e) {
			LOGGER.error("执行SQL更新异常：{}", sql, e);
			closeConnection();
			throw new RepositoryException(e);
		} finally {
			close(null, statement);
		}
		return ok;
	}
	
	/**
	 * 关闭资源
	 */
	public void shutdown() {
		LOGGER.info("数据库释放");
		try {
			this.closeConnection();
		} catch (Exception e) {
			LOGGER.error("数据库释放异常", e);
		}
	}
	
	/**
	 * 结果集封装
	 */
	private List<ResultSetWrapper> wrapperResultSet(ResultSet result) throws SQLException {
		final String[] columns = columnNames(result);
		final List<ResultSetWrapper> list = new ArrayList<>();
		while(result.next()) {
			final ResultSetWrapper wrapper = new ResultSetWrapper();
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
	private String[] columnNames(ResultSet result) throws SQLException {
		final ResultSetMetaData meta = result.getMetaData();
		final int count = meta.getColumnCount();
		final String[] columns = new String[count];
		for (int index = 0; index < count; index++) {
			columns[index] = meta.getColumnName(index + 1);
		}
		return columns;
	}

	/**
	 * 获取连接
	 */
	private Connection connection() throws SQLException {
		if(this.connection != null) {
			return this.connection;
		}
		synchronized (this) {
			if(this.connection == null) {
				try {
					Class.forName(DatabaseConfig.getDriver());
					this.connection = DriverManager.getConnection(DatabaseConfig.getUrl(), DatabaseConfig.getUser(), DatabaseConfig.getPassword());
				} catch (ClassNotFoundException | SQLException e) {
					LOGGER.error("打开JDBC连接异常", e);
				}
			}
		}
		return connection();
	}
	
	private void closeConnection() {
		try {
			if(this.connection != null && !this.connection.isClosed()) {
				this.connection.close();
			}
			this.connection = null;
		} catch (SQLException e) {
			LOGGER.error("JDBC连接关闭异常", e);
		}
	}
	
	private void close(ResultSet result, PreparedStatement statement) {
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
				LOGGER.error("JDBC处理器关闭异常", e);
			}
		}
	}
	
}
