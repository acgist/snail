package com.acgist.snail.net.torrent.lsd;

import java.net.StandardProtocolFamily;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.config.SystemConfig;
import com.acgist.snail.context.SystemThreadContext;
import com.acgist.snail.context.TorrentContext;
import com.acgist.snail.net.UdpServer;
import com.acgist.snail.utils.NetUtils;

/**
 * <p>本地发现服务端</p>
 * 
 * @author acgist
 */
public final class LocalServiceDiscoveryServer extends UdpServer<LocalServiceDiscoveryAcceptHandler> {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(LocalServiceDiscoveryServer.class);
	
	private static final LocalServiceDiscoveryServer INSTANCE = new LocalServiceDiscoveryServer();
	
	public static final LocalServiceDiscoveryServer getInstance() {
		return INSTANCE;
	}

	/**
	 * <p>LSD组播端口：{@value}</p>
	 */
	public static final int LSD_PORT = 6771;
	/**
	 * <p>LSD组播地址（IPv4）：{@value}</p>
	 */
	public static final String LSD_HOST = "239.192.152.143";
	/**
	 * <p>LSD组播地址（IPv6）：{@value}</p>
	 */
	public static final String LSD_HOST_IPV6 = "[ff15::efc0:988f]";
	
	/**
	 * <p>禁止创建实例</p>
	 */
	private LocalServiceDiscoveryServer() {
		super(LSD_PORT, true, "LSD Server", LocalServiceDiscoveryAcceptHandler.getInstance());
		this.join(TTL, lsdHost());
		this.handle();
		this.register();
	}

	/**
	 * <p>注册本地发现服务</p>
	 */
	private void register() {
		LOGGER.debug("注册本地发现服务：定时任务");
		final int interval = SystemConfig.getLsdInterval();
		SystemThreadContext.timerFixedDelay(
			interval,
			interval,
			TimeUnit.SECONDS,
			this::multicast
		);
	}
	
	/**
	 * <p>发送本地发现消息</p>
	 */
	private void multicast() {
		LOGGER.debug("发送本地发现消息");
		final LocalServiceDiscoveryClient client = LocalServiceDiscoveryClient.newInstance();
		TorrentContext.getInstance().allTorrentSession().forEach(session -> {
			if(session.privateTorrent()) {
				LOGGER.debug("私有种子（禁止发送本地发现消息）：{}", session.infoHashHex());
			} else {
				LOGGER.debug("发送本地发现消息：{}", session.infoHashHex());
				client.localSearch(session.infoHashHex());
			}
		});
	}
	
	/**
	 * <p>获取LSD组播地址</p>
	 * 
	 * @return LSD组播地址
	 */
	public static final String lsdHost() {
		if(NetUtils.LOCAL_PROTOCOL_FAMILY == StandardProtocolFamily.INET) {
			return LSD_HOST;
		} else {
			return LSD_HOST_IPV6;
		}
	}
	
}
