package com.acgist.snail.context.recycle;

import java.io.File;
import java.nio.file.Paths;

import com.acgist.snail.utils.StringUtils;

/**
 * <p>回收站</p>
 * 
 * @author acgist
 */
public abstract class Recycle {

	/**
	 * <p>删除文件路径</p>
	 * <p>必须完整路径（不能填写相对路径）</p>
	 */
	protected final String path;
	/**
	 * <p>删除文件</p>
	 */
	protected final File file;
	
	/**
	 * <p>回收站</p>
	 * 
	 * @param path 文件路径
	 */
	protected Recycle(String path) {
		if(StringUtils.isEmpty(path)) {
			throw new IllegalArgumentException("删除文件路径错误：" + path);
		}
		this.file = Paths.get(path).toFile();
		this.path = file.getAbsolutePath();
	}
	
	/**
	 * <p>删除文件</p>
	 * 
	 * @return 是否删除成功
	 */
	public abstract boolean delete();

}
