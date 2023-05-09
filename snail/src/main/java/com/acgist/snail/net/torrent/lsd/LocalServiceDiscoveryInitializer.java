package com.acgist.snail.net.torrent.lsd;

import com.acgist.snail.context.Initializer;

/**
 * 本地发现初始化器
 * 
 * @author acgist
 */
public final class LocalServiceDiscoveryInitializer extends Initializer {

	/**
	 * 延迟时间（秒）：{@value}
	 */
	private static final int DELAY = 10;
	
	private LocalServiceDiscoveryInitializer() {
		super("本地发现", DELAY);
	}
	
	public static final LocalServiceDiscoveryInitializer newInstance() {
		return new LocalServiceDiscoveryInitializer();
	}
	
	@Override
	protected void init() {
		LocalServiceDiscoveryServer.getInstance();
	}
	
	@Override
	protected void release() {
		LocalServiceDiscoveryServer.getInstance().close();
	}

}
