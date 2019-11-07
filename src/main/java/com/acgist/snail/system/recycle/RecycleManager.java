package com.acgist.snail.system.recycle;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.system.context.SystemContext;
import com.acgist.snail.system.recycle.window.WindowRecycle;

/**
 * 回收站管理器
 * 
 * @author acgist
 * @since 1.1.0
 */
public final class RecycleManager {

	private static final Logger LOGGER = LoggerFactory.getLogger(RecycleManager.class);
	
	/**
	 * 回收站创建器
	 */
	private static final Map<String, Function<String, Recycle>> BUILDER;
	
	static {
		LOGGER.info("初始化回收站");
		BUILDER = new HashMap<>();
		register("Windows 10", (path) -> {
			return new WindowRecycle(path);
		});
	}
	
	/**
	 * 创建回收站
	 */
	public static final Recycle newInstance(String path) {
		final var builder = BUILDER.get(SystemContext.osName());
		if(builder == null) {
			return null;
		}
		return builder.apply(path);
	}
	
	/**
	 * 注册回收站创建器
	 */
	public static final void register(String osName, Function<String, Recycle> builder) {
		LOGGER.info("注册回收站创建器：{}", osName);
		BUILDER.put(osName, builder);
	}
	
}
