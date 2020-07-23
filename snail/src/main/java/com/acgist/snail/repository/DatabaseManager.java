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
import com.acgist.snail.utils.CollectionUtils;

/**
 * <p>数据库管理器</p>
 * 
 * @author acgist
 * @since 1.0.0
 */
public final class DatabaseManager {

	private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseManager.class);
	
	private static final DatabaseManager INSTANCE = new DatabaseManager();
	
	public static final DatabaseManager getInstance() {
		return INSTANCE;
	}
	
	/**
	 * <p>数据库连接</p>
	 */
	private Connection connection;
	
	private DatabaseManager() {
	}

	/**
	 * <p>查询数据库是否初始化</p>
	 * 
	 * @return 是否初始化
	 */
	public boolean databaseInit() {
		final List<ResultSetWrapper> tables = this.select("show tables");
		return CollectionUtils.isNotEmpty(tables);
	}
	
	/**
	 * <p>查询</p>
	 * 
	 * @param sql SQL
	 * @param parameters 参数
	 * 
	 * @return 查询结果
	 */
	public List<ResultSetWrapper> select(String sql, Object ... parameters) {
		ResultSet result = null;
		PreparedStatement statement = null;
		try {
			final Connection connection = this.connection();
			statement = connection.prepareStatement(sql);
			if(ArrayUtils.isNotEmpty(parameters)) {
				for (int index = 0; index < parameters.length; index++) {
					statement.setObject(index + 1, BeanUtils.pack(parameters[index]));
				}
			}
			result = statement.executeQuery();
			return this.wrapperResultSet(result);
		} catch (SQLException e) {
			LOGGER.error("执行SQL查询异常：{}-{}", sql, parameters, e);
			this.closeConnection();
			throw new RepositoryException(e);
		} finally {
			this.close(result, statement);
		}
	}

	/**
	 * <p>更新</p>
	 * 
	 * @param sql SQL
	 * @param parameters 参数
	 * 
	 * @return 更新结果
	 */
	public boolean update(String sql, Object ... parameters) {
		boolean ok = false;
		PreparedStatement statement = null;
		try {
			final Connection connection = this.connection();
			statement = connection.prepareStatement(sql);
			if(ArrayUtils.isNotEmpty(parameters)) {
				for (int index = 0; index < parameters.length; index++) {
					statement.setObject(index + 1, BeanUtils.pack(parameters[index]));
				}
			}
			ok = statement.execute();
		} catch (SQLException e) {
			LOGGER.error("执行SQL更新异常：{}-{}", sql, parameters, e);
			this.closeConnection();
			throw new RepositoryException(e);
		} finally {
			this.close(null, statement);
		}
		return ok;
	}
	
	/**
	 * <p>关闭资源</p>
	 */
	public void shutdown() {
		LOGGER.info("释放数据库");
		try {
			this.closeConnection();
		} catch (Exception e) {
			LOGGER.error("释放数据库异常", e);
		}
	}
	
	/**
	 * <p>结果集包装器</p>
	 * 
	 * @param result 结果集
	 * 
	 * @return 结果集包装器
	 * 
	 * @throws SQLException SQL异常
	 */
	private List<ResultSetWrapper> wrapperResultSet(ResultSet result) throws SQLException {
		final String[] columns = this.columnNames(result);
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
	 * <p>获取列名</p>
	 * 
	 * @param result 结果集
	 * 
	 * @return 列名
	 * 
	 * @throws SQLException SQL异常
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
	 * <p>获取连接</p>
	 * 
	 * @return 连接
	 * 
	 * @throws SQLException SQL异常
	 */
	private Connection connection() throws SQLException {
		if(this.connection != null) {
			return this.connection;
		}
		synchronized (this) {
			if(this.connection == null) {
				try {
					Class.forName(DatabaseConfig.getDriver());
					this.connection = DriverManager.getConnection(
						DatabaseConfig.getUrl(),
						DatabaseConfig.getUser(),
						DatabaseConfig.getPassword()
					);
				} catch (ClassNotFoundException | SQLException e) {
					LOGGER.error("打开JDBC连接异常", e);
				}
			}
		}
		return this.connection();
	}
	
	/**
	 * <p>关闭连接</p>
	 */
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
	
	/**
	 * <p>关闭资源</p>
	 * 
	 * @param result 结果集
	 * @param statement 处理器
	 */
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
