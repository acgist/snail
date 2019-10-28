package com.acgist.snail.net.stun.bootstrap;

import java.net.InetSocketAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.net.stun.StunClient;
import com.acgist.snail.net.upnp.bootstrap.UpnpService;
import com.acgist.snail.system.config.StunConfig;
import com.acgist.snail.system.config.SystemConfig;
import com.acgist.snail.utils.NetUtils;
import com.acgist.snail.utils.StringUtils;

/**
 * Stun Service
 * 
 * @author acgist
 * @since 1.2.0
 */
public class StunService {

	private static final Logger LOGGER = LoggerFactory.getLogger(StunService.class);
	
	private static final StunService INSTANCE = new StunService();
	
	public static final StunService getInstance() {
		return INSTANCE;
	}

	/**
	 * 映射
	 */
	public void mapping() {
		if(UpnpService.getInstance().useable()) {
			LOGGER.info("UPNP映射成功：不使用STUN映射");
		} else {
			final var address = buildServerAddress();
			if(address == null) {
				return;
			}
			LOGGER.debug("STUN获取映射信息：{}", address);
			final var client = StunClient.newInstance(address);
			client.mappedAddress();
		}
	}
	
	/**
	 * 设置映射信息
	 * 
	 * @param externalIpAddress 外网IP地址
	 * @param port 端口号
	 */
	public void mapping(String externalIpAddress, int port) {
		SystemConfig.setExternalIpAddress(externalIpAddress);
		SystemConfig.setTorrentPortExt(port);
	}

	/**
	 * 设置STUN服务器地址
	 */
	private InetSocketAddress buildServerAddress() {
		final String server = SystemConfig.getStunServer();
		if(StringUtils.isEmpty(server)) {
			LOGGER.warn("STUN服务器错误：{}", server);
			return null;
		}
		final String[] values = server.split(":");
		if(values.length == 1) {
			return NetUtils.buildSocketAddress(values[0], StunConfig.DEFAULT_PORT);
		} else if(values.length == 2) {
			if(StringUtils.isNumeric(values[1])) {
				return NetUtils.buildSocketAddress(values[0], Integer.parseInt(values[1]));
			}
		} else if(values.length == 3) {
			if(StringUtils.isNumeric(values[2])) {
				return NetUtils.buildSocketAddress(values[1], Integer.parseInt(values[2]));
			}
		}
		LOGGER.warn("STUN服务器错误：{}", server);
		return null;
	}
	
}
