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
	
	public static final String REQUESTED_API_VERSION = "1.7.30";
	
	/**
	 * <p>SL4J绑定方法</p>
	 * 
	 * @return {@link StaticLoggerBinder}
	 */
	public static final StaticLoggerBinder getSingleton() {
		return INSTANCE;
	}
	
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
