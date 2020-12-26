package com.acgist.snail.context;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.config.SystemConfig;
import com.acgist.snail.net.stun.StunService;
import com.acgist.snail.net.upnp.UpnpClient;
import com.acgist.snail.net.upnp.UpnpServer;
import com.acgist.snail.net.upnp.UpnpService;
import com.acgist.snail.utils.NetUtils;

/**
 * <p>NAT（内网穿透）</p>
 * <p>已是公网IP不会进行内网穿透</p>
 * <p>优先使用UPNP进行端口映射，UPNP映射失败后使用STUN。</p>
 * 
 * @author acgist
 */
public final class NatContext {

	private static final Logger LOGGER = LoggerFactory.getLogger(NatContext.class);
	
	private static final NatContext INSTANCE = new NatContext();
	
	public static final NatContext getInstance() {
		return INSTANCE;
	}
	
	/**
	 * <p>内网穿透类型</p>
	 * 
	 * @author acgist
	 */
	public enum Type {
		
		/**
		 * <p>UPNP</p>
		 */
		UPNP,
		/**
		 * <p>STUN</p>
		 */
		STUN,
		/**
		 * <p>没有使用内网穿透</p>
		 */
		NONE;
		
	}
	
	/**
	 * <p>UPNP端口映射超时时间：{@value}</p>
	 */
	private static final int UPNP_TIMEOUT = SystemConfig.CONNECT_TIMEOUT_MILLIS;
	
	/**
	 * <p>内网穿透类型</p>
	 */
	private Type type = Type.NONE;
	/**
	 * <p>UPNP等待锁</p>
	 */
	private final Object upnpLock = new Object();
	
	/**
	 * <p>禁止创建实例</p>
	 */
	private NatContext() {
		this.register();
	}
	
	/**
	 * <p>注册NAT服务</p>
	 */
	private void register() {
		LOGGER.info("注册NAT服务");
		if(NetUtils.localIPAddress(NetUtils.LOCAL_HOST_ADDRESS)) {
			UpnpClient.newInstance().mSearch();
			this.lockUpnp();
			if(UpnpService.getInstance().useable()) {
				LOGGER.info("UPNP映射成功");
				this.type = Type.UPNP;
			} else {
				LOGGER.info("UPNP映射失败：使用STUN映射");
				this.type = Type.STUN;
				StunService.getInstance().mapping();
			}
		} else {
			LOGGER.info("已是公网IP地址：忽略NAT设置");
			SystemConfig.setExternalIpAddress(NetUtils.LOCAL_HOST_ADDRESS);
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
		if(this.type == Type.UPNP) {
			UpnpService.getInstance().release();
			UpnpServer.getInstance().close();
		}
	}

	/**
	 * <p>添加UPNP等待锁</p>
	 */
	private void lockUpnp() {
		synchronized (this.upnpLock) {
			try {
				this.upnpLock.wait(UPNP_TIMEOUT);
			} catch (InterruptedException e) {
				LOGGER.debug("线程等待异常", e);
				Thread.currentThread().interrupt();
			}
		}
	}
	
	/**
	 * <p>释放UPNP等待锁</p>
	 */
	public void unlockUpnp() {
		synchronized (this.upnpLock) {
			this.upnpLock.notifyAll();
		}
	}
	
}
