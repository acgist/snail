package com.acgist.snail.pojo.entity;

/**
 * entity - 任务
 */
public class TaskEntity extends BaseEntity {

	private static final long serialVersionUID = 1L;

	public static final String PROPERTY_NAME = "name"; // 任务名称
	
	/**
	 * 任务名称
	 */
	private String name;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
