package com.acgist.snail.net.torrent.local;

import java.net.InetSocketAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.net.UdpClient;
import com.acgist.snail.net.torrent.peer.bootstrap.PeerService;
import com.acgist.snail.net.upnp.UpnpServer;
import com.acgist.snail.pojo.wrapper.HeaderWrapper;
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
	
	private static final String PROTOCOL = "BT-SEARCH * HTTP/1.1";
	
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
		final String peerId = StringUtils.hex(PeerService.getInstance().peerId());
		final HeaderWrapper builder = HeaderWrapper.newBuilder(PROTOCOL);
		builder
			.header("Host", LocalServiceDiscoveryServer.LSD_HOST + ":" + LocalServiceDiscoveryServer.LSD_PORT)
			.header("Port", SystemConfig.getTorrentPort().toString())
			.header("Infohash", infoHash)
			.header("cookie", peerId);
		return builder.build();
	}
	
}
