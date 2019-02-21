package com.acgist.snail.repository;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import com.acgist.snail.pojo.entity.BaseEntity;

/**
 * 数据库
 */
public class BaseRepository<T extends BaseEntity> {

	protected String table;
	
	public BaseRepository(String table) {
		this.table = table;
	}

	protected void save(T t) {
		t.setId(UUID.randomUUID().toString());
		t.setCreateDate(new Date());
		t.setModifyDate(new Date());
	}
	
	protected void update(T t) {
		t.setModifyDate(new Date());
	}
	
	protected void delete(T t) {
		JDBCConnection.update(buildDeleteSQL(), t.getId());
	}

	private String buildDeleteSQL() {
		StringBuilder builder = new StringBuilder();
		builder
		.append("DELETE FROM ")
		.append(table)
		.append(" WHERE ID = ?");
		return builder.toString();
	}
	
	protected T findOne(String sql) {
		return null;
	}
	
	protected List<T> findList(String sql) {
		return null;
	}
	
}
