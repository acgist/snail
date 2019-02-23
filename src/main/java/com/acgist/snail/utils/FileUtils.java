package com.acgist.snail.utils;

import java.io.File;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 文件工具
 */
public class FileUtils {

	private static final Logger LOGGER = LoggerFactory.getLogger(FileUtils.class);

	/**
	 * 删除文件
	 */
	public static final void delete(String path) {
		if(StringUtils.isEmpty(path)) {
			return;
		}
		File file = new File(path);
		if(!file.exists()) {
			LOGGER.warn("删除文件不存在：{}", path);
			return;
		}
		LOGGER.info("删除文件：{}", path);
		file.delete();
	}
	
	/**
	 * 获取文件路径：如果文件路径存在返回文件路径，如果不存在获取user.dir路径+文件路径
	 */
	public static final String folderPath(String path) {
		File file = new File(path);
		if(file.exists()) {
			return path;
		}
		path = System.getProperty("user.dir") + path;
		file = new File(path);
		if(file.exists()) {
			return path;
		}
		file.mkdirs();
		return path;
	}
	
}
