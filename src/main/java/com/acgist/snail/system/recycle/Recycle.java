package com.acgist.snail.system.recycle;

import java.io.File;

import com.acgist.snail.system.exception.ArgumentException;
import com.acgist.snail.utils.StringUtils;

/**
 * 回收站
 * 
 * @author acgist
 * @since 1.1.0
 */
public abstract class Recycle {

	/**
	 * 原始文件路径
	 */
	protected final String path;
	/**
	 * 原始文件
	 */
	protected final File file;
	
	/**
	 * 回收站构造器
	 * 
	 * @param path 文件路径：必须是完整路径（不能填写相对路径）
	 */
	protected Recycle(String path) {
		if(StringUtils.isEmpty(path)) {
			throw new ArgumentException("回收站路径格式错误：" + path);
		}
		this.path = path;
		this.file = new File(path);
	}
	
	/**
	 * 删除文件
	 * 
	 * @return true-删除成功；false-删除失败；
	 */
	public abstract boolean delete();

}
