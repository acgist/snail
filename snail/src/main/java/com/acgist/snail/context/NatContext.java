package com.acgist.snail.context;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.IContext;
import com.acgist.snail.config.SystemConfig;
import com.acgist.snail.net.stun.StunService;
import com.acgist.snail.net.upnp.UpnpClient;
import com.acgist.snail.net.upnp.UpnpServer;
import com.acgist.snail.net.upnp.UpnpService;
import com.acgist.snail.utils.NetUtils;

/**
 * <p>NAT（内网穿透）上下文</p>
 * <p>已是公网IP不会进行内网穿透</p>
 * <p>优先使用UPNP进行端口映射，UPNP映射失败后使用STUN。</p>
 * 
 * @author acgist
 */
public final class NatContext implements IContext {

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
	 * <p>注册NAT执行周期</p>
	 * <p>如果NAT注册失败下次执行周期</p>
	 */
	private static final int NAT_INTERVAL = 10;
	
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
	}
	
	/**
	 * <p>注册NAT服务</p>
	 * <p>必须外部调用不能单例注册：导致不能唤醒</p>
	 */
	public void register() {
		if(this.type != Type.NONE) {
			LOGGER.debug("注册NAT服务成功：{}", this.type);
			return;
		}
		LOGGER.debug("注册NAT服务");
		if(NetUtils.localIP(NetUtils.LOCAL_HOST_ADDRESS)) {
			UpnpClient.newInstance().mSearch();
			this.lockUpnp();
			if(UpnpService.getInstance().useable()) {
				LOGGER.debug("UPNP映射成功");
				this.type = Type.UPNP;
			} else {
				LOGGER.debug("UPNP映射失败：使用STUN映射");
				StunService.getInstance().mapping();
			}
			if(this.type == Type.NONE) {
				LOGGER.info("注册NAT服务下次执行时间：{}", NAT_INTERVAL);
				SystemThreadContext.timer(NAT_INTERVAL, TimeUnit.SECONDS, this::register);
			}
		} else {
			LOGGER.debug("已是公网IP地址：忽略NAT设置");
			SystemConfig.setExternalIPAddress(NetUtils.LOCAL_HOST_ADDRESS);
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
	 * <p>设置STUN穿透类型</p>
	 */
	public void stun() {
		this.type = Type.STUN;
	}
	
	/**
	 * <p>关闭NAT服务</p>
	 */
	public void shutdown() {
		LOGGER.debug("关闭NAT服务");
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
				Thread.currentThread().interrupt();
				LOGGER.debug("线程等待异常", e);
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
