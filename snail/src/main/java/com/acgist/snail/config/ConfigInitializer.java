package com.acgist.snail.config;

import com.acgist.snail.context.Initializer;

/**
 * 初始化配置
 * 初始化下载必须的配置，可选下载功能的配置不在这里初始化。
 * 
 * @author acgist
 */
public final class ConfigInitializer extends Initializer {

    private ConfigInitializer() {
        super("配置");
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
    protected void destroyProxy() {
    }

}
