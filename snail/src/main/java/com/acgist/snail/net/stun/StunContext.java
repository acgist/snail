package com.acgist.snail.net.stun;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

import com.acgist.snail.config.PeerConfig;
import com.acgist.snail.config.StunConfig;
import com.acgist.snail.config.SymbolConfig;
import com.acgist.snail.config.SystemConfig;
import com.acgist.snail.context.IContext;
import com.acgist.snail.context.SystemThreadContext;
import com.acgist.snail.logger.Logger;
import com.acgist.snail.logger.LoggerFactory;
import com.acgist.snail.net.NatContext;
import com.acgist.snail.net.torrent.dht.NodeContext;
import com.acgist.snail.utils.NetUtils;
import com.acgist.snail.utils.StringUtils;

/**
 * <p>Stun上下文</p>
 * 
 * @author acgist
 */
public final class StunContext implements IContext {

	private static final Logger LOGGER = LoggerFactory.getLogger(StunContext.class);
	
	private static final StunContext INSTANCE = new StunContext();
	
	/**
	 * <p>配置多个服务器时轮询使用</p>
	 */
	private int index = 0;
	/**
	 * 服务器地址
	 */
	private String server;
	/**
	 * 是否启动定时任务
	 */
	private volatile boolean scheduled = false;
	/**
	 * <p>是否是否注册成功</p>
	 */
	private volatile boolean available = false;
	
	public static final StunContext getInstance() {
		return INSTANCE;
	}
	
	private StunContext() {
	}

	/**
	 * <p>判断是否注册成功</p>
	 * 
	 * @return 是否注册成功
	 */
	public boolean available() {
		return this.available;
	}
	
	/**
	 * <p>端口映射</p>
	 */
	public void mapping() {
		final var address = this.buildServerAddress();
		if(address == null) {
			LOGGER.warn("STUN端口映射地址无效");
			return;
		}
		StunClient.newInstance(address).mapping();
	}
	
	/**
	 * <p>设置端口映射信息</p>
	 * 
	 * @param externalIPAddress 外网IP地址
	 * @param port 外网端口
	 */
	public void mapping(String externalIPAddress, int port) {
		if(
			!this.available ||
			port != SystemConfig.getTorrentPortExt() ||
			!StringUtils.equals(externalIPAddress, SystemConfig.getExternalIPAddress())
		) {
			LOGGER.debug("STUN端口映射：{}-{}", externalIPAddress, port);
			this.available = true;
			PeerConfig.nat();
			SystemConfig.setTorrentPortExt(port);
			SystemConfig.setExternalIPAddress(externalIPAddress);
			NodeContext.getInstance().buildNodeId(externalIPAddress);
			NatContext.getInstance().unlock();
		} else {
			LOGGER.debug("STUN端口映射（没有变化）：{}-{}", externalIPAddress, port);
		}
		// 开启定时任务：端口保活
		if(!this.scheduled) {
			this.scheduled = true;
			final int interval = SystemConfig.getStunInterval();
			LOGGER.debug("启动STUN端口映射定时服务：{}", interval);
			SystemThreadContext.scheduledAtFixedRate(
				interval,
				interval,
				TimeUnit.SECONDS,
				this::mapping
			);
		}
	}

	/**
	 * <p>获取STUN服务器地址</p>
	 * 
	 * @return STUN服务器地址
	 */
	private InetSocketAddress buildServerAddress() {
		final String server = SystemConfig.getStunServer();
		if(StringUtils.isEmpty(server)) {
			LOGGER.warn("STUN服务器列表格式错误：{}", server);
			return null;
		}
		final String[] servers = SymbolConfig.Symbol.COMMA.split(server);
		if(!this.available) {
			final int index = Math.abs(this.index++ % servers.length);
			this.server = servers[index];
		}
		return this.buildServerAddress(this.server);
	}
	
	/**
	 * 获取STUN服务器地址，支持格式：
	 * 
	 * stun1.l.google.com
	 * stun:stun1.l.google.com
	 * stun1.l.google.com:19302
	 * stun:stun1.l.google.com:19302
	 * 
	 * @param server STUN服务器地址列表
	 * 
	 * @return STUN服务器地址
	 */
	private InetSocketAddress buildServerAddress(String server) {
		LOGGER.debug("STUN服务器地址：{}", server);
		final String[] args = SymbolConfig.Symbol.COLON.split(server);
		final int argLength = args.length;
		final String lastArg = args[argLength - 1];
		if(argLength == 0) {
			LOGGER.warn("STUN服务器格式错误：{}", server);
			return null;
		} else if(argLength == 1) {
			return NetUtils.buildSocketAddress(lastArg, StunConfig.DEFAULT_PORT);
		} else if(StringUtils.isNumeric(lastArg)) {
			return NetUtils.buildSocketAddress(args[argLength - 2], Integer.parseInt(lastArg));
		} else {
			return NetUtils.buildSocketAddress(lastArg, StunConfig.DEFAULT_PORT);
		}
	}
	
}
