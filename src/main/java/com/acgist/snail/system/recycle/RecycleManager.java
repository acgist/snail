package com.acgist.snail.system.recycle;

import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.system.context.SystemContext;
import com.acgist.snail.system.recycle.windows.WindowsRecycle;

/**
 * <p>回收站管理器</p>
 * 
 * @author acgist
 * @since 1.1.0
 */
public final class RecycleManager {

	private static final Logger LOGGER = LoggerFactory.getLogger(RecycleManager.class);
	
	/**
	 * <p>回收站</p>
	 */
	private static final Function<String, Recycle> BUILDER;
	
	static {
		final String osName = SystemContext.osName();
		LOGGER.info("初始化回收站：{}", osName);
		if(WindowsRecycle.support(osName)) {
			BUILDER = (path) -> new WindowsRecycle(path);
		} else {
			BUILDER = null;
		}
	}
	
	/**
	 * <p>创建回收站</p>
	 */
	public static final Recycle newInstance(String path) {
		if(BUILDER == null) {
			return null;
		}
		return BUILDER.apply(path);
	}
	
}
