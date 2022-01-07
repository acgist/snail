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
		 * <p>公网IP</p>
		 */
		OPEN,
		/**
		 * <p>没有使用内网穿透</p>
		 */
		NONE;
		
	}
	
	/**
	 * <p>端口映射超时时间（毫秒）：{@value}</p>
	 */
	private static final long MAPPING_TIMEOUT = 2L * SystemConfig.ONE_SECOND_MILLIS;
	/**
	 * <p>注册NAT服务执行周期（秒）：{@value}</p>
	 */
	private static final int NAT_INTERVAL = 4;
	
	/**
	 * <p>内网穿透类型</p>
	 */
	private Type type = Type.NONE;
	/**
	 * <p>端口映射等待锁</p>
	 */
	private final Object lock = new Object();
	
	private NatContext() {
	}
	
	/**
	 * <p>注册NAT服务</p>
	 * <p>必须外部调用不能单例注册：导致不能唤醒</p>
	 * <p>公网IP地址不用穿透，优先使用UPNP进行端口映射，如果映射失败使用STUN穿透。</p>
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
				SystemThreadContext.timer(NAT_INTERVAL, TimeUnit.SECONDS, this::register);
			}
		} else {
			LOGGER.debug("注册NAT服务成功：已是公网IP地址");
			this.type = Type.OPEN;
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
	 * <p>设置UPNP穿透类型</p>
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
	 * <p>设置STUN穿透类型</p>
	 * 
	 * @return 是否成功
	 */
	public boolean stun() {
		// 如果已经可用直接返回
		if(StunContext.getInstance().available()) {
			return true;
		}
		StunContext.getInstance().mapping();
		this.lock();
		return StunContext.getInstance().available();
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
	 * <p>添加端口映射等待锁</p>
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
	 * <p>释放端口映射等待锁</p>
	 */
	public void unlock() {
		synchronized (this.lock) {
			this.lock.notifyAll();
		}
	}

}
