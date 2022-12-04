package com.acgist.snail.config;

import com.acgist.snail.context.Initializer;

/**
 * 配置初始化器
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

}
