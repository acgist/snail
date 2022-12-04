package com.acgist.snail.context.entity;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

import com.acgist.snail.utils.BeanUtils;
import com.acgist.snail.utils.StringUtils;

/**
 * <p>Entity - 实体</p>
 * 
 * @author acgist
 */
public abstract class Entity implements Serializable {

	private static final long serialVersionUID = 1L;

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
		return Objects.hash(this.id);
	}
	
	@Override
	public boolean equals(Object object) {
		if(this == object) {
			return true;
		}
		if(object instanceof Entity entity) {
			return StringUtils.equals(this.id, entity.id);
		}
		return false;
	}
	
	@Override
	public String toString() {
		return BeanUtils.toString(this);
	}
	
}
