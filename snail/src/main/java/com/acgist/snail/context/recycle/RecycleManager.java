package com.acgist.snail.context.recycle;

import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.context.SystemContext.SystemType;
import com.acgist.snail.context.recycle.windows.WindowsRecycle;
import com.acgist.snail.utils.StringUtils;

/**
 * <p>回收站管理器</p>
 * 
 * @author acgist
 */
public final class RecycleManager {

	private static final Logger LOGGER = LoggerFactory.getLogger(RecycleManager.class);
	
	/**
	 * <p>回收站创建器</p>
	 */
	private static final Function<String, Recycle> BUILDER;
	
	static {
		final SystemType systemType = SystemType.local();
		LOGGER.debug("初始化回收站：{}", systemType);
		if(systemType == SystemType.WINDOWS) {
			BUILDER = WindowsRecycle::new;
		} else {
			LOGGER.warn("不支持回收站：{}", systemType);
			BUILDER = null;
		}
	}
	
	/**
	 * <p>禁止创建实例</p>
	 */
	private RecycleManager() {
	}
	
	/**
	 * <p>创建回收站</p>
	 * 
	 * @param path 文件路径
	 * 
	 * @return 回收站
	 */
	public static final Recycle newInstance(String path) {
		if(BUILDER == null) {
			return null;
		}
		return BUILDER.apply(path);
	}
	
	/**
	 * <p>使用回收站删除文件</p>
	 * 
	 * @param filePath 文件路径
	 * 
	 * @return 是否删除成功
	 */
	public static final boolean recycle(final String filePath) {
		if(StringUtils.isEmpty(filePath)) {
			LOGGER.warn("删除文件路径错误：{}", filePath);
			return false;
		}
		final var recycle = RecycleManager.newInstance(filePath);
		if(recycle == null) {
			// 不支持回收站
			return false;
		}
		return recycle.delete();
	}
	
}
