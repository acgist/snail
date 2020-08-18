package com.acgist.snail.system.context;

import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.net.stun.bootstrap.StunService;
import com.acgist.snail.net.upnp.UpnpClient;
import com.acgist.snail.net.upnp.UpnpServer;
import com.acgist.snail.net.upnp.bootstrap.UpnpService;
import com.acgist.snail.system.config.SystemConfig;
import com.acgist.snail.utils.NetUtils;
import com.acgist.snail.utils.ThreadUtils;

/**
 * <p>NAT（内网穿透）</p>
 * <p>已是公网IP不会进行内网穿透</p>
 * <p>优先使用UPNP进行端口映射，UPNP映射失败后使用STUN。</p>
 * 
 * @author acgist
 * @since 1.2.0
 */
public final class NatContext {

	private static final Logger LOGGER = LoggerFactory.getLogger(NatContext.class);
	
	private static final NatContext INSTANCE = new NatContext();
	
	public static final NatContext getInstance() {
		return INSTANCE;
	}
	
	/**
	 * <p>内网穿透类型</p>
	 */
	public enum Type {
		
		/** UPNP */
		UPNP,
		/** STUN */
		STUN,
		/** 没有使用内网穿透 */
		NONE;
		
	}
	
	/**
	 * <p>UPNP端口映射超时时间</p>
	 */
	private static final int UPNP_CONFIG_TIMEOUT = SystemConfig.CONNECT_TIMEOUT;
	
	/**
	 * <p>UPNP等待锁</p>
	 */
	private final Object upnpLock = new Object();
	
	/**
	 * <p>内网穿透类型</p>
	 */
	private Type type = Type.NONE;
	
	private NatContext() {
	}
	
	/**
	 * <p>初始化NAT服务</p>
	 */
	public void init() {
		LOGGER.info("初始化NAT服务");
		if(NetUtils.isLocalIp(NetUtils.localHostAddress())) {
			UpnpClient.newInstance().mSearch();
			synchronized (this.upnpLock) {
				ThreadUtils.wait(this.upnpLock, Duration.ofSeconds(UPNP_CONFIG_TIMEOUT));
			}
			if(UpnpService.getInstance().useable()) {
				LOGGER.info("UPNP映射成功：不使用STUN映射");
				this.type = Type.UPNP;
			} else {
				LOGGER.info("UPNP映射失败：使用STUN映射");
				this.type = Type.STUN;
				StunService.getInstance().mapping();
			}
		} else {
			LOGGER.info("已是公网IP地址忽略NAT设置");
		}
	}
	
	/**
	 * <p>获取内网穿透类型</p>
	 * 
	 * @return 内网穿透类型
	 */
	public Type type() {
		return this.type;
	}
	
	/**
	 * <p>关闭NAT服务</p>
	 */
	public void shutdown() {
		LOGGER.info("关闭NAT服务");
		if(NetUtils.isLocalIp(NetUtils.localHostAddress())) {
			UpnpService.getInstance().release();
			UpnpServer.getInstance().close();
		}
	}

	/**
	 * <p>释放UPNP等待锁</p>
	 */
	public void unlock() {
		synchronized (this.upnpLock) {
			this.upnpLock.notifyAll();
		}
	}
	
}
