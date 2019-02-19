package com.acgist.snail.pojo.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * entity - 任务
 */
@Entity
@Table(name = "tb_task")
public class TaskEntity extends BaseEntity {

	private static final long serialVersionUID = 1L;

	public static final String PROPERTY_NAME = "name"; // 任务名称
	
	/**
	 * 任务名称
	 */
	private String name;

	@Column(nullable = false, length = 100)
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
