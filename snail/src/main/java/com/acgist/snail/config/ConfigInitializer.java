package com.acgist.snail.config;

import com.acgist.snail.context.Initializer;

/**
 * 初始化系统配置
 * 初始化下载必须的系统配置，可选的功能配置不在这里初始化。
 * 
 * @author acgist
 */
public final class ConfigInitializer extends Initializer {

    private ConfigInitializer() {
        super("系统配置");
    }

    public static final ConfigInitializer newInstance() {
        return new ConfigInitializer();
    }

    @Override
    protected void init() {
        SystemConfig.getInstance();
        DownloadConfig.getInstance();
    }

    @Override
    protected void release() {
    }

}
