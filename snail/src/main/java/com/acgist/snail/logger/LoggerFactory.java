package com.acgist.snail.logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.acgist.snail.config.SymbolConfig;
import com.acgist.snail.logger.adapter.ConsoleLoggerAdapter;
import com.acgist.snail.logger.adapter.FileLoggerAdapter;

/**
 * 日志工厂
 * 
 * @author acgist
 */
public final class LoggerFactory {

    private static final LoggerFactory INSTANCE = new LoggerFactory();

    public static final LoggerFactory getInstance() {
        return INSTANCE;
    }

    /**
     * 错误日志文件
     */
    private static final String ERROR_LOG_FILE = "logs/snail.error.log";
    
    /**
     * 日志对象
     */
    private final Map<String, Logger> loggers;
    /**
     * 日志适配器
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
                } else {
                    // 不支持的适配器
                }
            }
        }
        this.adapters = list;
    }
    
    /**
     * @param clazz class
     * 
     * @return 日志
     */
    public static final Logger getLogger(Class<?> clazz) {
        return INSTANCE.loggers.computeIfAbsent(clazz.getName(), Logger::new);
    }

    /**
     * @return 日志适配器
     */
    public static final List<LoggerAdapter> getAdapters() {
        return INSTANCE.adapters;
    }

    /**
     * 系统异常记录
     * 
     * @param t 异常
     */
    public static final void error(Throwable t) {
        try(
            final OutputStream outputStream = new FileOutputStream(new File(ERROR_LOG_FILE), true);
            final PrintWriter printWriter   = new PrintWriter(outputStream);
        ) {
            t.printStackTrace(printWriter);
            printWriter.flush();
        } catch (Exception e) {
            error(t);
        }
    }
    
    /**
     * 关闭日志
     */
    public static final void shutdown() {
        INSTANCE.adapters.forEach(LoggerAdapter::release);
    }

}
