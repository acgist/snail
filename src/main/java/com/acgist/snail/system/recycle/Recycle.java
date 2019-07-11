package com.acgist.snail.system.recycle;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.system.context.SystemContext;
import com.acgist.snail.system.exception.ArgumentException;
import com.acgist.snail.system.recycle.window.WindowRecycle;
import com.acgist.snail.utils.StringUtils;

/**
 * 回收站
 * 
 * @author acgist
 * @since 1.1.0
 */
public abstract class Recycle {

	private static final Logger LOGGER = LoggerFactory.getLogger(Recycle.class);
	
	static {
		LOGGER.info("初始化回收站");
		BUILDER = new HashMap<>();
		register("Windows 7", (path) -> {
			return new WindowRecycle(path);
		});
		register("Windows 10", (path) -> {
			return new WindowRecycle(path);
		});
	}
	
	/**
	 * 源文件路径
	 */
	protected final String path;
	/**
	 * 源文件
	 */
	protected final File file;
	/**
	 * 回收站创建器
	 */
	private static final Map<String, Function<String, Recycle>> BUILDER;
	
	/**
	 * 回收站构造器
	 * 
	 * @param path 文件路径，必须是完整路径，不能填写相对路径。
	 */
	protected Recycle(String path) {
		if(StringUtils.isEmpty(path)) {
			throw new ArgumentException("回收站路径不能为空");
		}
		this.path = path;
		this.file = new File(path);
	}
	
	/**
	 * 创建回收站
	 */
	public static final Recycle newInstance(String path) {
		return BUILDER.get(SystemContext.osName()).apply(path);
	}
	
	/**
	 * 注册回收站创建器
	 */
	public static final void register(String osName, Function<String, Recycle> builder) {
		LOGGER.info("注册回收站创建器：{}", osName);
		BUILDER.put(osName, builder);
	}
	
	/**
	 * 删除文件
	 * 
	 * @return true-删除成功；false-删除失败；
	 */
	public abstract boolean delete();

}
