package com.acgist.snail.pojo.entity;

/**
 * entity - 配置
 */
public class ConfigEntity extends BaseEntity {

	private static final long serialVersionUID = 1L;

	public static final String TABLE_NAME = "tb_config";
	
	public static final String PROPERTY_NAME = "name"; // 配置名称
	public static final String PROPERTY_VALUE = "value"; // 配置值

	/**
	 * 配置名称
	 */
	private String name;
	/**
	 * 配置值
	 */
	private String value;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

}
