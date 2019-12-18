package com.acgist.snail.pojo.entity;

import java.io.Serializable;
import java.util.Date;

import com.acgist.snail.utils.ObjectUtils;
import com.acgist.snail.utils.StringUtils;

/**
 * <p>Entity - 基类</p>
 * 
 * @author acgist
 * @since 1.0.0
 */
public class BaseEntity implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * <p>ID</p>
	 */
	public static final String PROPERTY_ID = "id";
	/**
	 * <p>创建时间</p>
	 */
	public static final String PROPERTY_CREATE_DATE = "createDate";
	/**
	 * <p>修改时间</p>
	 */
	public static final String PROPERTY_MODIFY_DATE = "modifyDate";
	
	/**
	 * <p>ID</p>
	 */
	protected String id;
	/**
	 * <p>创建时间</p>
	 */
	protected Date createDate;
	/**
	 * <p>修改时间</p>
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
	 * @return 创建时间
	 */
	public Date getCreateDate() {
		return createDate;
	}

	/**
	 * @param createDate 创建时间
	 */
	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}

	/**
	 * @return 修改时间
	 */
	public Date getModifyDate() {
		return modifyDate;
	}

	/**
	 * @param modifyDate 修改时间
	 */
	public void setModifyDate(Date modifyDate) {
		this.modifyDate = modifyDate;
	}

	@Override
	public int hashCode() {
		return ObjectUtils.hashCode(this.id);
	}
	
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
	
	@Override
	public String toString() {
		return ObjectUtils.toString(this);
	}
	
}
