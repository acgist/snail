package com.acgist.snail.context;

import java.util.concurrent.TimeUnit;

import com.acgist.snail.IContext;
import com.acgist.snail.config.SystemConfig;
import com.acgist.snail.context.exception.NetException;
import com.acgist.snail.logger.Logger;
import com.acgist.snail.logger.LoggerFactory;
import com.acgist.snail.net.upnp.UpnpClient;
import com.acgist.snail.net.upnp.UpnpServer;
import com.acgist.snail.utils.NetUtils;

/**
 * NAT（内网穿透）上下文
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
	 * 内网穿透类型
	 * 
	 * @author acgist
	 */
	public enum Type {
		
		/**
		 * UPNP
		 */
		UPNP,
		/**
		 * STUN
		 */
		STUN,
		/**
		 * 公网IP
		 */
		OPEN,
		/**
		 * 没有使用内网穿透
		 */
		NONE;
		
	}
	
	/**
	 * 端口映射超时时间（毫秒）：{@value}
	 */
	private static final long MAPPING_TIMEOUT = 2L * SystemConfig.ONE_SECOND_MILLIS;
	/**
	 * 注册NAT服务执行周期（秒）：{@value}
	 */
	private static final int NAT_INTERVAL = 5;
	
	/**
	 * 内网穿透类型
	 */
	private Type type = Type.NONE;
	/**
	 * 端口映射等待锁
	 */
	private final Object lock = new Object();
	
	private NatContext() {
	}
	
	/**
	 * 注册NAT服务
	 * 必须外部调用不能单例注册：导致不能唤醒
	 * 公网IP地址不用穿透，优先使用UPNP进行端口映射，如果UPNP映射失败使用STUN穿透。
	 */
	public void register() {
		if(NetUtils.localIP(NetUtils.LOCAL_HOST_ADDRESS)) {
			// 本地IP地址
			if(this.upnp()) {
				this.type = Type.UPNP;
				LOGGER.debug("注册NAT服务成功：{}", this.type);
			} else if(this.stun()) {
				this.type = Type.STUN;
				LOGGER.debug("注册NAT服务成功：{}", this.type);
			} else {
				LOGGER.debug("注册NAT服务失败：{}", NAT_INTERVAL);
				SystemThreadContext.scheduled(NAT_INTERVAL, TimeUnit.SECONDS, this::register);
			}
		} else {
			LOGGER.debug("注册NAT服务成功：已是公网IP地址");
			this.type = Type.OPEN;
			SystemConfig.setTorrentPortExt(SystemConfig.getTorrentPort());
			SystemConfig.setExternalIPAddress(NetUtils.LOCAL_HOST_ADDRESS);
			NodeContext.getInstance().buildNodeId(NetUtils.LOCAL_HOST_ADDRESS);
		}
	}
	
	/**
	 * @return 内网穿透类型
	 */
	public Type type() {
		return this.type;
	}
	
	/**
	 * 设置UPNP穿透类型
	 * 
	 * @return 是否成功
	 */
	public boolean upnp() {
		// 没有准备完成发送消息进入等待
		if(!UpnpContext.getInstance().available()) {
			UpnpClient.newInstance().mSearch();
			this.lock();
		}
		// 已经准备成功直接映射
		if(UpnpContext.getInstance().available()) {
			try {
				return UpnpContext.getInstance().mapping();
			} catch (NetException e) {
				LOGGER.error("设置UPNP穿透异常", e);
			}
		}
		return false;
	}
	
	/**
	 * 设置STUN穿透类型
	 * 
	 * @return 是否成功
	 */
	public boolean stun() {
		// 如果已经可用直接返回
		if(StunContext.getInstance().available()) {
			return true;
		}
		// 发送端口映射
		StunContext.getInstance().mapping();
		this.lock();
		return StunContext.getInstance().available();
	}
	
	/**
	 * 关闭NAT服务
	 */
	public void shutdown() {
		LOGGER.debug("关闭NAT服务：{}", this.type);
		if(this.type == Type.UPNP) {
			UpnpContext.getInstance().release();
			UpnpServer.getInstance().close();
		}
	}

	/**
	 * 添加端口映射等待锁
	 */
	public void lock() {
		synchronized (this.lock) {
			try {
				this.lock.wait(MAPPING_TIMEOUT);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				LOGGER.debug("线程等待异常", e);
			}
		}
	}
	
	/**
	 * 释放端口映射等待锁
	 */
	public void unlock() {
		synchronized (this.lock) {
			this.lock.notifyAll();
		}
	}

}
