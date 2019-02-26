package com.acgist.snail.repository;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.module.exception.RepositoryException;
import com.acgist.snail.pojo.entity.BaseEntity;
import com.acgist.snail.pojo.wrapper.ResultSetWrapper;
import com.acgist.snail.utils.CollectionUtils;
import com.acgist.snail.utils.EntityUtils;
import com.acgist.snail.utils.JSONUtils;

/**
 * 数据库
 */
public abstract class BaseRepository<T extends BaseEntity> {

	private static final Logger LOGGER = LoggerFactory.getLogger(BaseRepository.class);
	
	/**
	 * 数据库列：只允许字符串
	 */
	private static final Function<String, String> COLUMN = (value) -> {
		if(value.matches("[a-zA-Z]+")) {
			return value;
		}
		throw new RepositoryException("数据库列格式错误");
	};
	
	protected String table;
	
	protected BaseRepository(String table) {
		this.table = table;
	}
	
	public void save(T t) {
		if(t == null) {
			throw new RepositoryException("保存参数异常：" + t);
		}
		t.setId(UUID.randomUUID().toString());
		t.setCreateDate(new Date());
		t.setModifyDate(new Date());
		final String[] properties = EntityUtils.entityProperty(t.getClass());
		final String sqlProperty = Stream.of(properties)
			.map(property -> "`" + property + "`")
			.collect(Collectors.joining(",", "(", ")"));
		final String sqlValue = Stream.of(properties)
			.map(property -> "?")
			.collect(Collectors.joining(",", "(", ")"));
		final Object[] parameters = Stream.of(properties)
			.map(property -> EntityUtils.entityPropertyValue(t, property))
			.toArray();
		final StringBuilder sql = new StringBuilder();
		sql
			.append("INSERT INTO ")
			.append(table)
			.append(sqlProperty)
			.append(" VALUES ")
			.append(sqlValue);
		if(LOGGER.isDebugEnabled()) {
			LOGGER.debug("SQL语句：{}", sql);
			LOGGER.debug("SQL参数：{}", JSONUtils.javaToJson(parameters));
		}
		JDBCConnection.update(sql.toString(), parameters);
	}

	public void update(T t) {
		if(t == null) {
			throw new RepositoryException("修改参数异常：" + t);
		}
		t.setModifyDate(new Date());
		final String[] properties = EntityUtils.entityProperty(t.getClass());
		final String sqlProperty = Stream.of(properties)
			.filter(property -> {
				return
					!BaseEntity.PROPERTY_ID.equals(property) &&
					!BaseEntity.PROPERTY_CREATE_DATE.equals(property);
			})
			.map(property -> "`" + property + "` = ?")
			.collect(Collectors.joining(","));
		final Object[] parameters = Stream.concat(
				Stream.of(properties)
					.filter(property -> {
						return
							!BaseEntity.PROPERTY_ID.equals(property) &&
							!BaseEntity.PROPERTY_CREATE_DATE.equals(property);
					})
					.map(property -> EntityUtils.entityPropertyValue(t, property)),
				Stream.of(t.getId())
			).toArray();
		final StringBuilder sql = new StringBuilder();
		sql
			.append("UPDATE ")
			.append(table)
			.append(" SET ")
			.append(sqlProperty)
			.append(" WHERE ID = ?");
		if(LOGGER.isDebugEnabled()) {
			LOGGER.debug("SQL语句：{}", sql);
			LOGGER.debug("SQL参数：{}", JSONUtils.javaToJson(parameters));
		}
		JDBCConnection.update(sql.toString(), parameters);
	}
	
	public void delete(String id) {
		if(id == null) {
			throw new RepositoryException("删除参数异常：" + id);
		}
		StringBuilder sql = new StringBuilder();
		sql
			.append("DELETE FROM ")
			.append(table)
			.append(" WHERE ID = ?");
		JDBCConnection.update(sql.toString(), id);
	}

	public T findOne(String id) {
		if(id == null) {
			throw new RepositoryException("查询参数异常：" + id);
		}
		StringBuilder sql = new StringBuilder();
		sql
			.append("SELECT * FROM ")
			.append(table)
			.append(" WHERE ID = ? limit 1");
		List<ResultSetWrapper> list = JDBCConnection.select(sql.toString(), id);
		if(list == null || list.isEmpty()) {
			return null;
		}
		T t = newInstance();
		EntityUtils.entity(t, list.get(0));
		return t;
	}
	
	public T findOne(String property, String value) {
		if(property == null) {
			throw new RepositoryException("查询参数异常：" + property);
		}
		StringBuilder sql = new StringBuilder();
		sql
			.append("SELECT * FROM ")
			.append(table)
			.append(" WHERE ")
			.append(COLUMN.apply(property))
			.append(" = ? limit 1");
		List<ResultSetWrapper> list = JDBCConnection.select(sql.toString(), value);
		if(list == null || list.isEmpty()) {
			return null;
		}
		T t = newInstance();
		EntityUtils.entity(t, list.get(0));
		return t;
	}
	
	public List<T> findList(String sql, Object ... parameters) {
		if(sql == null) {
			throw new RepositoryException("查询参数异常：" + sql);
		}
		List<ResultSetWrapper> list = JDBCConnection.select(sql, parameters);
		if(list == null || list.isEmpty()) {
			return null;
		}
		return list
			.stream()
			.map(wrapper -> {
				T t = newInstance();
				EntityUtils.entity(t, wrapper);
				return t;
			})
			.collect(Collectors.toList());
	}
	
	public List<T> findAll() {
		StringBuilder sql = new StringBuilder();
		sql
			.append("SELECT * FROM ")
			.append(table);
		List<ResultSetWrapper> list = JDBCConnection.select(sql.toString());
		if(list == null || list.isEmpty()) {
			return null;
		}
		return list
			.stream()
			.map(wrapper -> {
				T t = newInstance();
				EntityUtils.entity(t, wrapper);
				return t;
			})
			.collect(Collectors.toList());
	}
	
	/**
	 * 新实体
	 */
	private T newInstance() {
		Class<T> clazz = entityClazz();
		try {
			return clazz.getDeclaredConstructor().newInstance();
		} catch (Exception e) {
			LOGGER.error("反射异常", e);
		}
		return null;
	}
	
	/**
	 * 获取泛型
	 * TODO：泛型优化
	 */
	@SuppressWarnings("unchecked")
	private Class<T> entityClazz() {
		Class<T> entitiClazz = null;
		Type superClazz = this.getClass().getGenericSuperclass();
		if (superClazz instanceof ParameterizedType) {
			Type[] types = ((ParameterizedType) superClazz).getActualTypeArguments();
			if (CollectionUtils.isNotEmpty(types)) {
				entitiClazz = (Class<T>) types[0];
			}
		}
		return entitiClazz;
	}
	
}
	
