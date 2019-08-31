package com.acgist.snail.net.torrent.local;

import java.net.InetSocketAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.net.UdpClient;
import com.acgist.snail.net.torrent.peer.bootstrap.PeerService;
import com.acgist.snail.net.upnp.UpnpServer;
import com.acgist.snail.system.config.SystemConfig;
import com.acgist.snail.system.exception.NetException;
import com.acgist.snail.utils.NetUtils;
import com.acgist.snail.utils.StringUtils;

/**
 * <p>本地发现客户端</p>
 * <p>执行周期和PEX定时任务一致，启动时不执行。</p>
 * 
 * @author acgist
 * @since 1.1.0
 */
public class LocalServiceDiscoveryClient extends UdpClient<LocalServiceDiscoveryMessageHandler> {

	private static final Logger LOGGER = LoggerFactory.getLogger(LocalServiceDiscoveryClient.class);
	
	private static final String NEW_LINE = "\r\n";
	
	public LocalServiceDiscoveryClient(InetSocketAddress socketAddress) {
		super("LSD Client", new LocalServiceDiscoveryMessageHandler(), socketAddress);
	}

	public static final LocalServiceDiscoveryClient newInstance() {
		return new LocalServiceDiscoveryClient(NetUtils.buildSocketAddress(LocalServiceDiscoveryServer.LSD_HOST, LocalServiceDiscoveryServer.LSD_PORT));
	}

	@Override
	public boolean open() {
		return this.open(UpnpServer.getInstance().channel());
	}
	
	/**
	 * 发送本地发现消息
	 */
	public void localSearch(String infoHash) {
		LOGGER.debug("本地发现，InfoHash：{}", infoHash);
		try {
			send(buildLocalSearch(infoHash));
		} catch (NetException e) {
			LOGGER.error("发送本地发现消息异常", e);
		}
	}
	
	/**
	 * 构建本地发现消息
	 */
	private String buildLocalSearch(String infoHash) {
		final StringBuilder builder = new StringBuilder();
		final String peerId = StringUtils.hex(PeerService.getInstance().peerId());
		builder
			.append("BT-SEARCH * HTTP/1.1").append(NEW_LINE)
			.append("Host: ").append(LocalServiceDiscoveryServer.LSD_HOST).append(":").append(LocalServiceDiscoveryServer.LSD_PORT).append(NEW_LINE)
			.append("Port: ").append(SystemConfig.getTorrentPort()).append(NEW_LINE)
			.append("Infohash: ").append(infoHash).append(NEW_LINE)
			.append("cookie: ").append(peerId).append(NEW_LINE) // 过滤本机使用
			.append(NEW_LINE);
		return builder.toString();
	}
	
}
