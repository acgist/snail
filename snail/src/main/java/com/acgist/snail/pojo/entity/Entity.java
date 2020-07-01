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
public class Entity implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * <p>ID：{@value}</p>
	 */
	public static final String PROPERTY_ID = "id";
	/**
	 * <p>创建时间：{@value}</p>
	 */
	public static final String PROPERTY_CREATE_DATE = "createDate";
	/**
	 * <p>修改时间：{@value}</p>
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
	 * <p>获取ID</p>
	 * 
	 * @return ID
	 */
	public String getId() {
		return id;
	}

	/**
	 * <p>设置ID</p>
	 * 
	 * @param id ID
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * <p>获取创建时间</p>
	 * 
	 * @return 创建时间
	 */
	public Date getCreateDate() {
		return createDate;
	}

	/**
	 * <p>设置创建时间</p>
	 * 
	 * @param createDate 创建时间
	 */
	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}

	/**
	 * <p>获取修改时间</p>
	 * 
	 * @return 修改时间
	 */
	public Date getModifyDate() {
		return modifyDate;
	}

	/**
	 * <p>设置修改时间</p>
	 * 
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
		if(object instanceof Entity) {
			final Entity entity = (Entity) object;
			return StringUtils.equals(this.id, entity.id);
		}
		return false;
	}
	
	@Override
	public String toString() {
		return ObjectUtils.toString(this);
	}
	
}
