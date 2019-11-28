package com.acgist.snail.pojo.entity;

import java.io.Serializable;
import java.util.Date;

import com.acgist.snail.utils.ObjectUtils;
import com.acgist.snail.utils.StringUtils;

/**
 * Entity - 基类
 * 
 * @author acgist
 * @since 1.0.0
 */
public class BaseEntity implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * ID
	 */
	public static final String PROPERTY_ID = "id";
	/**
	 * 创建时间
	 */
	public static final String PROPERTY_CREATE_DATE = "createDate";
	/**
	 * 修改时间
	 */
	public static final String PROPERTY_MODIFY_DATE = "modifyDate";
	
	/**
	 * ID
	 */
	protected String id;
	/**
	 * 创建日期
	 */
	protected Date createDate;
	/**
	 * 修改日期
	 */
	protected Date modifyDate;
	
	/**
	 * @return ID
	 */
	public String getId() {
		return id;
	}

	/**
	 * <p>自动设置</p>
	 * 
	 * @param id ID
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * @return 创建日期
	 */
	public Date getCreateDate() {
		return createDate;
	}

	/**
	 * @param createDate 创建日期
	 */
	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}

	/**
	 * @return 修改日期
	 */
	public Date getModifyDate() {
		return modifyDate;
	}

	/**
	 * @param modifyDate 修改日期
	 */
	public void setModifyDate(Date modifyDate) {
		this.modifyDate = modifyDate;
	}

	/**
	 * 重写hashCode方法
	 */
	@Override
	public int hashCode() {
		return ObjectUtils.hashCode(this.id);
	}
	
	/**
	 * 重写equals方法
	 */
	@Override
	public boolean equals(Object object) {
		if(ObjectUtils.equals(this, object)) {
			return true;
		}
		if(object instanceof BaseEntity) {
			final BaseEntity entity = (BaseEntity) object;
			return StringUtils.equals(this.id, entity.id);
		}
		return false;
	}
	
	/**
	 * 重写toString方法
	 */
	@Override
	public String toString() {
		return ObjectUtils.toString(this);
	}
	
}
