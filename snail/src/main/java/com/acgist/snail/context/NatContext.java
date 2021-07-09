package com.acgist.snail.context;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.IContext;
import com.acgist.snail.config.SystemConfig;
import com.acgist.snail.net.upnp.UpnpClient;
import com.acgist.snail.net.upnp.UpnpServer;
import com.acgist.snail.utils.NetUtils;

/**
 * <p>NAT（内网穿透）上下文</p>
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
	 * <p>UPNP端口映射超时时间（毫秒）：{@value}</p>
	 */
	private static final long UPNP_TIMEOUT = 4L * SystemConfig.ONE_SECOND_MILLIS;
	/**
	 * <p>注册NAT服务执行周期（秒）：{@value}</p>
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
	
	private NatContext() {
	}
	
	/**
	 * <p>注册NAT服务</p>
	 * <p>必须外部调用不能单例注册：导致不能唤醒</p>
	 * <p>公网IP地址不用穿透，优先使用UPNP进行端口映射，如果映射失败使用STUN穿透。</p>
	 */
	public void register() {
		if(this.type != Type.NONE) {
			LOGGER.debug("注册NAT服务成功：{}", this.type);
			return;
		}
		if(NetUtils.localIP(NetUtils.LOCAL_HOST_ADDRESS)) {
			UpnpClient.newInstance().mSearch();
			this.lockUpnp();
			if(UpnpContext.getInstance().useable()) {
				this.type = Type.UPNP;
			} else {
				StunContext.getInstance().mapping();
			}
			if(this.type == Type.NONE) {
				LOGGER.debug("注册NAT服务失败：{}", NAT_INTERVAL);
				SystemThreadContext.timer(NAT_INTERVAL, TimeUnit.SECONDS, this::register);
			} else {
				LOGGER.debug("注册NAT服务成功：{}", this.type);
			}
		} else {
			LOGGER.debug("注册NAT服务成功：已是公网IP地址");
			SystemConfig.setExternalIPAddress(NetUtils.LOCAL_HOST_ADDRESS);
			NodeContext.getInstance().buildNodeId(NetUtils.LOCAL_HOST_ADDRESS);
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
			UpnpContext.getInstance().release();
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
