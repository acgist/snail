package com.acgist.snail.system.context;

import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.net.stun.bootstrap.StunService;
import com.acgist.snail.net.upnp.UpnpClient;
import com.acgist.snail.net.upnp.UpnpServer;
import com.acgist.snail.net.upnp.bootstrap.UpnpService;
import com.acgist.snail.system.config.SystemConfig;
import com.acgist.snail.utils.ThreadUtils;

/**
 * <p>NAT（内网穿透）</p>
 * <p>优先使用UPNP，失败后使用STUN。</p>
 * 
 * @author acgist
 * @since 1.2.0
 */
public class NatContext {

	private static final Logger LOGGER = LoggerFactory.getLogger(NatContext.class);
	
	private static final NatContext INSTANCE = new NatContext();
	
	/**
	 * UPNP配置超时时间
	 */
	private static final int UPNP_CONFIG_TIMEOUT = SystemConfig.CONNECT_TIMEOUT;
	
	/**
	 * UPNP等待锁
	 */
	private final Object upnpLock = new Object();
	
	public static final NatContext getInstance() {
		return INSTANCE;
	}
	
	/**
	 * 初始化NAT服务
	 */
	public void init() {
		LOGGER.info("初始化NAT服务");
		UpnpClient.newInstance().mSearch();
		synchronized (this.upnpLock) {
			ThreadUtils.wait(this.upnpLock, Duration.ofSeconds(UPNP_CONFIG_TIMEOUT));
		}
		StunService.getInstance().mapping();
	}
	
	/**
	 * 关闭NAT服务
	 */
	public void shutdown() {
		LOGGER.info("关闭NAT服务");
		UpnpService.getInstance().release();
		UpnpServer.getInstance().close();
	}

	/**
	 * 释放锁
	 */
	public void unlock() {
		synchronized (this.upnpLock) {
			this.upnpLock.notifyAll();
		}
	}
	
}
