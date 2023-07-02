package com.acgist.snail.logger;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 日志工具
 * 
 * @author acgist
 */
public final class Logger {

    /**
     * 默认字符长度
     */
    private static final int DEFAULT_CAPACITY = 128;
    /**
     * 时间格式
     */
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    /**
     * 日志级别
     */
    private final int level;
    /**
     * 日志名称
     */
    private final String name;
    /**
     * 日志名称格式
     */
    private final String nameFormat;
    /**
     * 日志系统名称
     */
    private final String system;
    /**
     * 日志系统格式
     */
    private final String systemFormat;
    /**
     * 日志上下文
     */
    private final List<LoggerAdapter> adapters;
    /**
     * 日志单元
     */
    private final Map<String, Tuple> tupleMap;
    
    /**
     * @param name 日志名称
     */
    public Logger(String name) {
        this.name         = name;
        this.nameFormat   = String.format(" %s ", this.buildSimpleName(this.name));
        this.level        = LoggerConfig.getLevel();
        this.system       = LoggerConfig.getSystem();
        this.systemFormat = String.format("[%s] ", this.system);
        this.adapters     = LoggerFactory.getAdapters();
        this.tupleMap     = new ConcurrentHashMap<>();
    }
    
    /**
     * 日志格式化
     * 
     * @param level  级别
     * @param format 格式
     * @param args   参数
     * 
     * @return 日志
     */
    private String format(Level level, String format, Object ... args) {
        final Tuple tuple = this.tupleMap.computeIfAbsent(format, Tuple::new);
        final StringBuilder builder = new StringBuilder(DEFAULT_CAPACITY);
        builder
            .append(this.systemFormat)
            .append(DATE_TIME_FORMATTER.format(LocalDateTime.now()))
            .append(" [")
            .append(Thread.currentThread().getName())
            .append("] ")
            .append(level)
            .append(this.nameFormat);
        tuple.format(builder, args).append("\n");
        final Throwable throwable = tuple.throwable(args);
        if(throwable != null) {
            final StringWriter stringWriter = new StringWriter();
            final PrintWriter printWriter   = new PrintWriter(stringWriter);
            throwable.printStackTrace(printWriter);
            builder
                .append(stringWriter.toString())
                .append("\n");
        }
        return builder.toString();
    }
    
    /**
     * 判断是否支持日志级别
     * 
     * @param level 级别
     * 
     * @return 是否支持
     */
    private boolean isEnabled(Level level) {
        return this.level <= level.value();
    }
    
    /**
     * 记录日志
     * 
     * @param level  级别
     * @param format 日志
     * @param args   参数
     */
    private void log(Level level, String format, Object ... args) {
        if(this.isEnabled(level)) {
            final String message = this.format(level, format, args);
            final boolean error  = level.value() >= Level.ERROR.value();
            if (error) {
                this.adapters.forEach(adapter -> adapter.errorOutput(message));
            } else {
                this.adapters.forEach(adapter -> adapter.output(message));
            }
        }
    }
    
    /**
     * 简化日志名称
     * 
     * @param name 完整名称
     * 
     * @return 简版名称
     */
    public String buildSimpleName(String name) {
        int old;
        int index      = 0;
        final char dot = '.';
        final StringBuilder builder = new StringBuilder();
        while((old = name.indexOf(dot, index)) != -1) {
            builder.append(name.substring(index, index + 1)).append(dot);
            index = old + 1;
        }
        builder.append(name.substring(index));
        return builder.toString();
    }
    
    /**
     * @return 是否支持DEBUG级别
     */
    public boolean isDebugEnabled() {
        return this.isEnabled(Level.DEBUG);
    }

    /**
     * 记录DEBUG日志
     * 
     * @param format 格式
     * @param args   参数
     */
    public void debug(String format, Object ... args) {
        this.log(Level.DEBUG, format, args);
    }

    /**
     * @return 是否支持INFO级别
     */
    public boolean isInfoEnabled() {
        return this.isEnabled(Level.INFO);
    }

    /**
     * 记录INFO日志
     * 
     * @param format 格式
     * @param args   参数
     */
    public void info(String format, Object ... args) {
        this.log(Level.INFO, format, args);
    }
    
    /**
     * @return 是否支持WARN级别
     */
    public boolean isWarnEnabled() {
        return this.isEnabled(Level.WARN);
    }

    /**
     * 记录WARN日志
     * 
     * @param format 格式
     * @param args   参数
     */
    public void warn(String format, Object ... args) {
        this.log(Level.WARN, format, args);
    }
    
    /**
     * @return 是否支持ERROR级别
     */
    public boolean isErrorEnabled() {
        return this.isEnabled(Level.ERROR);
    }

    /**
     * 记录ERROR日志
     * 
     * @param format 格式
     * @param args   参数
     */
    public void error(String format, Object ... args) {
        this.log(Level.ERROR, format, args);
    }
    
}
