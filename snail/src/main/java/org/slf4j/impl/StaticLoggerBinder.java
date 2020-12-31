package org.slf4j.impl;

import org.slf4j.ILoggerFactory;
import org.slf4j.spi.LoggerFactoryBinder;

import com.acgist.snail.logger.LoggerContext;

/**
 * <p>日志绑定</p>
 * 
 * @author acgist
 */
public final class StaticLoggerBinder implements LoggerFactoryBinder {

	private static final StaticLoggerBinder INSTANCE = new StaticLoggerBinder();
	
	public static final StaticLoggerBinder getInstance() {
		return INSTANCE;
	}
	
	/**
	 * <p>获取单例对象</p>
	 * 
	 * @return 单例对象
	 */
	public static final StaticLoggerBinder getSingleton() {
		return INSTANCE;
	}
	
	/**
	 * <p>禁止创建实例</p>
	 */
	private StaticLoggerBinder() {
	}
	
	@Override
	public ILoggerFactory getLoggerFactory() {
		return LoggerContext.getInstance();
	}

	@Override
	public String getLoggerFactoryClassStr() {
		return LoggerContext.getInstance().getName();
	}

}
