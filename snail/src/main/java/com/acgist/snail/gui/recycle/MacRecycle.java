package com.acgist.snail.gui.recycle;

import java.io.File;
import java.nio.file.Paths;

import com.acgist.snail.logger.Logger;
import com.acgist.snail.logger.LoggerFactory;

/**
 * <p>Mac回收站</p>
 * <p>删除时移动到废纸篓目录</p>
 * <p>TODO：优化测试</p>
 * 
 * @author acgist
 */
public class MacRecycle extends Recycle {

	private static final Logger LOGGER = LoggerFactory.getLogger(MacRecycle.class);
	
	/**
	 * <p>废纸篓目录：{@value}</p>
	 */
	private static final String TRASH_PATH = "~/.Trash";
	
	/**
	 * <p>Mac回收站</p>
	 * 
	 * @param path 删除文件路径
	 */
	public MacRecycle(String path) {
		super(path);
	}

	@Override
	public boolean delete() {
		final File source = this.file;
		final File target = Paths.get(TRASH_PATH, source.getName()).toFile();
		LOGGER.info("删除文件：{}-{}", source, target);
		return source.renameTo(target);
	}

}
