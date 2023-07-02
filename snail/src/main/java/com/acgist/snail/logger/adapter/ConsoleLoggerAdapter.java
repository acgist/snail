package com.acgist.snail.logger.adapter;

import com.acgist.snail.logger.LoggerAdapter;

/**
 * 控制台适配器
 * 
 * @author acgist
 */
public final class ConsoleLoggerAdapter extends LoggerAdapter {

    /**
     * 控制台适配器名称：{@value}
     */
    public static final String ADAPTER = "console";
    
    public ConsoleLoggerAdapter() {
        super(System.out, System.err);
    }
    
}
