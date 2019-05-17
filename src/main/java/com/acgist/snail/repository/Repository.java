package com.acgist.snail.repository;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.pojo.entity.BaseEntity;
import com.acgist.snail.pojo.wrapper.ResultSetWrapper;
import com.acgist.snail.system.exception.RepositoryException;
import com.acgist.snail.system.manager.DatabaseManager;
import com.acgist.snail.utils.BeanUtils;
import com.acgist.snail.utils.CollectionUtils;
import com.acgist.snail.utils.StringUtils;

/**
 * 数据库
 */
public abstract class Repository<T extends BaseEntity> {

	private static final Logger LOGGER = LoggerFactory.getLogger(Repository.class);
	
	private static final String COLUMN_REGEX = "[a-zA-Z]+";
	
	private DatabaseManager databaseManager = DatabaseManager.getInstance();
	
	/**
	 * 数据库列：只允许字符串
	 */
	private static final Function<String, String> COLUMN = (value) -> {
		if(StringUtils.regex(value, COLUMN_REGEX, true)) {
			return value;
		}
		throw new RepositoryException("数据库列格式错误");
	};
	
	protected String table;
	
	protected Repository(String table) {
		this.table = table;
	}
	
	public void save(T t) {
		if(t == null) {
			throw new RepositoryException("保存参数错误：" + t);
		}
		t.setId(UUID.randomUUID().toString());
		t.setCreateDate(new Date());
		t.setModifyDate(new Date());
		final String[] properties = BeanUtils.properties(t.getClass());
		final String sqlProperty = Stream.of(properties)
			.map(property -> "`" + property + "`")
			.collect(Collectors.joining(",", "(", ")"));
		final String sqlValue = Stream.of(properties)
			.map(property -> "?")
			.collect(Collectors.joining(",", "(", ")"));
		final Object[] parameters = Stream.of(properties)
			.map(property -> BeanUtils.propertyValue(t, property))
			.toArray();
		final StringBuilder sql = new StringBuilder();
		sql
			.append("INSERT INTO ")
			.append(this.table)
			.append(sqlProperty)
			.append(" VALUES ")
			.append(sqlValue);
		if(LOGGER.isDebugEnabled()) {
			LOGGER.debug("SQL语句：{}", sql);
			LOGGER.debug("SQL参数：{}", Arrays.asList(parameters));
		}
		this.databaseManager.update(sql.toString(), parameters);
	}

	public void update(T t) {
		if(t == null) {
			throw new RepositoryException("修改参数错误：" + t);
		}
		t.setModifyDate(new Date());
		final String[] properties = BeanUtils.properties(t.getClass());
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
					.map(property -> BeanUtils.propertyValue(t, property)),
				Stream.of(t.getId())
			).toArray();
		final StringBuilder sql = new StringBuilder();
		sql
			.append("UPDATE ")
			.append(this.table)
			.append(" SET ")
			.append(sqlProperty)
			.append(" WHERE ID = ?");
		if(LOGGER.isDebugEnabled()) {
			LOGGER.debug("SQL语句：{}", sql);
			LOGGER.debug("SQL参数：{}", Arrays.asList(parameters));
		}
		this.databaseManager.update(sql.toString(), parameters);
	}
	
	public void delete(String id) {
		if(id == null) {
			throw new RepositoryException("删除参数错误：" + id);
		}
		final StringBuilder sql = new StringBuilder();
		sql
			.append("DELETE FROM ")
			.append(this.table)
			.append(" WHERE ID = ?");
		this.databaseManager.update(sql.toString(), id);
	}

	public T findOne(String id) {
		if(id == null) {
			throw new RepositoryException("查询参数错误：" + id);
		}
		final StringBuilder sql = new StringBuilder();
		sql
			.append("SELECT * FROM ")
			.append(this.table)
			.append(" WHERE ID = ? limit 1");
		final List<ResultSetWrapper> list = this.databaseManager.select(sql.toString(), id);
		if(list == null || list.isEmpty()) {
			return null;
		}
		T t = newInstance();
		BeanUtils.entity(t, list.get(0));
		return t;
	}
	
	public T findOne(String property, String value) {
		if(property == null) {
			throw new RepositoryException("查询参数错误：" + property);
		}
		final StringBuilder sql = new StringBuilder();
		sql
			.append("SELECT * FROM ")
			.append(this.table)
			.append(" WHERE ")
			.append(COLUMN.apply(property))
			.append(" = ? limit 1");
		final List<ResultSetWrapper> list = this.databaseManager.select(sql.toString(), value);
		if(list == null || list.isEmpty()) {
			return null;
		}
		T t = newInstance();
		BeanUtils.entity(t, list.get(0));
		return t;
	}
	
	public List<T> findList(String sql, Object ... parameters) {
		if(sql == null) {
			throw new RepositoryException("查询参数错误：" + sql);
		}
		final List<ResultSetWrapper> list = this.databaseManager.select(sql, parameters);
		if(list == null || list.isEmpty()) {
			return null;
		}
		return list
			.stream()
			.map(wrapper -> {
				T t = newInstance();
				BeanUtils.entity(t, wrapper);
				return t;
			})
			.collect(Collectors.toList());
	}
	
	public List<T> findAll() {
		final StringBuilder sql = new StringBuilder();
		sql
			.append("SELECT * FROM ")
			.append(this.table);
		final List<ResultSetWrapper> list = this.databaseManager.select(sql.toString());
		if(list == null || list.isEmpty()) {
			return null;
		}
		return list
			.stream()
			.map(wrapper -> {
				T t = newInstance();
				BeanUtils.entity(t, wrapper);
				return t;
			})
			.collect(Collectors.toList());
	}
	
	/**
	 * 新实体
	 */
	private T newInstance() {
		return BeanUtils.newInstance(entityClazz());
	}
	
	/**
	 * 获取泛型
	 * TODO：泛型优化
	 */
	@SuppressWarnings("unchecked")
	private Class<T> entityClazz() {
		Class<T> entityClazz = null;
		final Type superClazz = this.getClass().getGenericSuperclass();
		if (superClazz instanceof ParameterizedType) {
			final Type[] types = ((ParameterizedType) superClazz).getActualTypeArguments();
			if (CollectionUtils.isNotEmpty(types)) {
				entityClazz = (Class<T>) types[0];
			}
		}
		return entityClazz;
	}

}
	
