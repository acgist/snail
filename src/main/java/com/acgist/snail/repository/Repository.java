package com.acgist.snail.repository;

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
import com.acgist.snail.utils.BeanUtils;
import com.acgist.snail.utils.CollectionUtils;
import com.acgist.snail.utils.StringUtils;

/**
 * 数据库
 * 
 * @author acgist
 * @since 1.0.0
 */
public abstract class Repository<T extends BaseEntity> {

	private static final Logger LOGGER = LoggerFactory.getLogger(Repository.class);
	
	/**
	 * 字段正则表达式
	 */
	private static final String COLUMN_REGEX = "[a-zA-Z]+";
	/**
	 * 数据库列验证
	 */
	private static final Function<String, String> COLUMN_VERIFY = (value) -> {
		if(StringUtils.regex(value, COLUMN_REGEX, true)) {
			return value;
		}
		throw new RepositoryException("数据库列格式错误：" + value);
	};
	
	private final DatabaseManager databaseManager = DatabaseManager.getInstance();
	
	/**
	 * 数据库表名
	 */
	private String table;
	/**
	 * <p>查询实体的类型</p>
	 * <p>注：不使用反射获取泛型，因为反射获取泛型时存在警告。</p>
	 */
	private final Class<T> entityClazz;
	
	protected Repository(String table, Class<T> entityClazz) {
		this.table = table;
		this.entityClazz = entityClazz;
	}
	
	/**
	 * <dl>
	 * 	<dt>合并：</dt>
	 * 	<dd>存在ID：更新</dd>
	 * 	<dd>不存在ID：保存</dd>
	 * </dl>
	 */
	public void merge(T t) {
		if(t == null) {
			throw new RepositoryException("合并参数错误：" + t);
		}
		if(t.getId() == null) {
			save(t);
		} else {
			update(t);
		}
	}
	
	/**
	 * 保存
	 */
	public void save(T t) {
		if(t == null) {
			throw new RepositoryException("保存参数错误：" + t);
		}
		if(t.getId() != null) {
			throw new RepositoryException("保存参数错误（ID）：" + t.getId());
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
			LOGGER.debug("保存SQL语句：{}", sql);
			LOGGER.debug("保存SQL参数：{}", Arrays.asList(parameters));
		}
		this.databaseManager.update(sql.toString(), parameters);
	}

	/**
	 * <p>更新</p>
	 * <p>更新所有字段</p>
	 */
	public void update(T t) {
		if(t == null) {
			throw new RepositoryException("修改参数错误：" + t);
		}
		if(t.getId() == null) {
			throw new RepositoryException("修改参数错误（ID）：" + t.getId());
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
			LOGGER.debug("更新SQL语句：{}", sql);
			LOGGER.debug("更新SQL参数：{}", Arrays.asList(parameters));
		}
		this.databaseManager.update(sql.toString(), parameters);
	}
	
	/**
	 * 删除
	 */
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

	/**
	 * 查找
	 */
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
		if(CollectionUtils.isEmpty(list)) {
			return null;
		}
		T t = newInstance();
		BeanUtils.setProperties(t, list.get(0));
		return t;
	}
	
	/**
	 * 查找
	 */
	public T findOne(String property, String value) {
		if(property == null) {
			throw new RepositoryException("查询参数错误：" + property);
		}
		final StringBuilder sql = new StringBuilder();
		sql
			.append("SELECT * FROM ")
			.append(this.table)
			.append(" WHERE ")
			.append(COLUMN_VERIFY.apply(property))
			.append(" = ? limit 1");
		final List<ResultSetWrapper> list = this.databaseManager.select(sql.toString(), value);
		if(CollectionUtils.isEmpty(list)) {
			return null;
		}
		T t = newInstance();
		BeanUtils.setProperties(t, list.get(0));
		return t;
	}
	
	/**
	 * 查找
	 */
	public List<T> findList(String sql, Object ... parameters) {
		if(sql == null) {
			throw new RepositoryException("查询参数错误：" + sql);
		}
		final List<ResultSetWrapper> list = this.databaseManager.select(sql, parameters);
		if(CollectionUtils.isEmpty(list)) {
			return List.of();
		}
		return list.stream()
			.map(wrapper -> {
				T t = newInstance();
				BeanUtils.setProperties(t, wrapper);
				return t;
			})
			.collect(Collectors.toList());
	}
	
	/**
	 * 查找
	 */
	public List<T> findAll() {
		final StringBuilder sql = new StringBuilder();
		sql
			.append("SELECT * FROM ")
			.append(this.table);
		final List<ResultSetWrapper> list = this.databaseManager.select(sql.toString());
		if(CollectionUtils.isEmpty(list)) {
			return List.of();
		}
		return list.stream()
			.map(wrapper -> {
				T t = newInstance();
				BeanUtils.setProperties(t, wrapper);
				return t;
			})
			.collect(Collectors.toList());
	}
	
	/**
	 * 新建实体
	 */
	private T newInstance() {
		return BeanUtils.newInstance(this.entityClazz);
	}
	
}
