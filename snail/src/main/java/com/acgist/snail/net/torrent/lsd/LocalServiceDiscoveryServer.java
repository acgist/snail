package com.acgist.snail.net.torrent.lsd;

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
	private static final String LSD_HOST_IPV4 = "239.192.152.143";
	/**
	 * <p>LSD组播地址（IPv6）：{@value}</p>
	 */
	private static final String LSD_HOST_IPV6 = "[ff15::efc0:988f]";
	
	private LocalServiceDiscoveryServer() {
		super(LSD_PORT, ADDR_REUSE, "LSD Server", LocalServiceDiscoveryAcceptHandler.getInstance());
		this.join(TTL, lsdHost());
		this.handle();
		final int interval = SystemConfig.getLsdInterval();
		SystemThreadContext.timerAtFixedDelay(
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
		if(NetUtils.localIPv4()) {
			return LSD_HOST_IPV4;
		} else {
			return LSD_HOST_IPV6;
		}
	}
	
}
