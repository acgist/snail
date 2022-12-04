package com.acgist.snail.context.entity;

import java.util.Objects;

import com.acgist.snail.utils.StringUtils;

/**
 * <p>Entity - 配置</p>
 * 
 * @author acgist
 */
public final class ConfigEntity extends Entity {

	private static final long serialVersionUID = 1L;

	/**
	 * <p>配置名称</p>
	 */
	private String name;
	/**
	 * <p>配置值</p>
	 */
	private String value;

	/**
	 * <p>获取配置名称</p>
	 * 
	 * @return 配置名称
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * <p>设置配置名称</p>
	 * 
	 * @param name 配置名称
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * <p>获取配置值</p>
	 * 
	 * @return 配置值
	 */
	public String getValue() {
		return this.value;
	}

	/**
	 * <p>设置配置值</p>
	 * 
	 * @param value 配置值
	 */
	public void setValue(String value) {
		this.value = value;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(this.id);
	}
	
	@Override
	public boolean equals(Object object) {
		if(this == object) {
			return true;
		}
		if(object instanceof ConfigEntity entity) {
			return StringUtils.equals(this.id, entity.id);
		}
		return false;
	}
	
}
