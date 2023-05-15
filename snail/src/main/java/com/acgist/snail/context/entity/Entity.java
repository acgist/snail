package com.acgist.snail.context.entity;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;
import java.util.UUID;

import com.acgist.snail.utils.BeanUtils;
import com.acgist.snail.utils.StringUtils;

/**
 * 实体
 * 
 * @author acgist
 */
public abstract class Entity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * ID
     * 
     * @see UUID
     */
    protected String id;
    /**
     * 创建时间
     */
    protected Date createDate;
    /**
     * 修改时间
     */
    protected Date modifyDate;
    
    /**
     * @return ID
     */
    public String getId() {
        return this.id;
    }

    /**
     * @param id ID
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * @return 创建时间
     */
    public Date getCreateDate() {
        return this.createDate;
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
        return this.modifyDate;
    }

    /**
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
