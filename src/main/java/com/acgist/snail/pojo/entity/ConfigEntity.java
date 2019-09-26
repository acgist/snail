package com.acgist.snail.pojo.entity;

/**
 * Entity - 配置
 * 
 * @author acgist
 * @since 1.0.0
 */
public class ConfigEntity extends BaseEntity {

	private static final long serialVersionUID = 1L;

	/**
	 * 配置表名
	 */
	public static final String TABLE_NAME = "tb_config";
	/**
	 * 配置名称
	 */
	public static final String PROPERTY_NAME = "name";
	/**
	 * 配置值
	 */
	public static final String PROPERTY_VALUE = "value";

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
