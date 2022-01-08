package com.acgist.snail.logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.acgist.snail.config.SymbolConfig;
import com.acgist.snail.logger.adapter.ConsoleLoggerAdapter;
import com.acgist.snail.logger.adapter.FileLoggerAdapter;

/**
 * <p>日志工厂</p>
 * 
 * @author acgist
 */
public final class LoggerFactory {

	private static final LoggerFactory INSTANCE = new LoggerFactory();

	public static final LoggerFactory getInstance() {
		return INSTANCE;
	}

	/**
	 * <p>错误日志文件</p>
	 */
	private static final String ERROR_LOG_FILE = "logs/snail.error.log";
	
	/**
	 * <p>日志对象</p>
	 */
	private final Map<String, Logger> loggers;
	/**
	 * <p>日志适配器</p>
	 */
	private final List<LoggerAdapter> adapters;
	
	private LoggerFactory() {
		this.loggers = new ConcurrentHashMap<>();
		final String adapter = LoggerConfig.getAdapter();
		final List<LoggerAdapter> list = new ArrayList<>();
		if(adapter != null && !adapter.isEmpty()) {
			final String[] adapters = SymbolConfig.Symbol.COMMA.split(adapter);
			for (String value : adapters) {
				value = value.strip();
				if(FileLoggerAdapter.ADAPTER.equalsIgnoreCase(value)) {
					list.add(new FileLoggerAdapter());
				} else if(ConsoleLoggerAdapter.ADAPTER.equalsIgnoreCase(value)) {
					list.add(new ConsoleLoggerAdapter());
				}
			}
		}
		this.adapters = list;
	}
	
	/**
	 * <p>获取日志</p>
	 * 
	 * @param clazz class
	 * 
	 * @return 日志
	 */
	public static final Logger getLogger(Class<?> clazz) {
		return INSTANCE.loggers.computeIfAbsent(clazz.getName(), Logger::new);
	}

	/**
	 * <p>获取日志适配器</p>
	 * 
	 * @return 日志适配器
	 */
	public static final List<LoggerAdapter> getAdapters() {
		return INSTANCE.adapters;
	}

	/**
	 * <p>系统异常记录</p>
	 * 
	 * @param t 异常
	 */
	public static final void error(Throwable t) {
		try(
			final var outputStream = new FileOutputStream(new File(ERROR_LOG_FILE), true);
			final var printWriter = new PrintWriter(outputStream);
		) {
			t.printStackTrace(printWriter);
			printWriter.flush();
		} catch (Exception e) {
			error(t);
		}
	}
	
	/**
	 * <p>关闭日志</p>
	 */
	public static final void shutdown() {
		INSTANCE.adapters.forEach(LoggerAdapter::release);
	}

}
