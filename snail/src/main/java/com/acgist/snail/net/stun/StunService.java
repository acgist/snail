package com.acgist.snail.net.stun;

import java.net.InetSocketAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.config.PeerConfig;
import com.acgist.snail.config.StunConfig;
import com.acgist.snail.config.SymbolConfig;
import com.acgist.snail.config.SystemConfig;
import com.acgist.snail.context.NatContext;
import com.acgist.snail.context.NodeContext;
import com.acgist.snail.utils.NetUtils;
import com.acgist.snail.utils.StringUtils;

/**
 * <p>Stun Service</p>
 * 
 * @author acgist
 */
public final class StunService {

	private static final Logger LOGGER = LoggerFactory.getLogger(StunService.class);
	
	private static final StunService INSTANCE = new StunService();
	
	/**
	 * <p>配置多个服务器时轮询使用</p>
	 */
	private int index = 0;
	
	public static final StunService getInstance() {
		return INSTANCE;
	}
	
	private StunService() {
	}

	/**
	 * <p>端口映射</p>
	 */
	public void mapping() {
		final var address = this.buildServerAddress();
		if(address == null) {
			return;
		}
		StunClient.newInstance(address).mappedAddress();
	}
	
	/**
	 * <p>设置端口映射信息</p>
	 * 
	 * @param externalIPAddress 外网IP地址
	 * @param port 外网端口
	 */
	public void mapping(String externalIPAddress, int port) {
		LOGGER.debug("STUN端口映射：{}-{}", externalIPAddress, port);
		PeerConfig.nat();
		NatContext.getInstance().stun();
		SystemConfig.setExternalIPAddress(externalIPAddress);
		NodeContext.getInstance().buildNodeId(externalIPAddress);
		SystemConfig.setTorrentPortExt(port);
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
		final String[] servers = server.split(SymbolConfig.Symbol.COMMA.toString());
		final int index = Math.abs(this.index++ % servers.length);
		return this.buildServerAddress(servers[index]);
	}
	
	/**
	 * <p>获取STUN服务器地址</p>
	 * 
	 * <table border="1">
	 * 	<caption>支持格式</caption>
	 * 	<tr>
	 * 		<th>格式</th>
	 * 	</tr>
	 * 	<tr>
	 * 		<td>stun1.l.google.com</td>
	 * 	</tr>
	 * 	<tr>
	 * 		<td>stun:stun1.l.google.com</td>
	 * 	</tr>
	 * 	<tr>
	 * 		<td>stun1.l.google.com:19302</td>
	 * 	</tr>
	 * 	<tr>
	 * 		<td>stun:stun1.l.google.com:19302</td>
	 * 	</tr>
	 * </table>
	 * 
	 * @param server STUN服务器地址
	 * 
	 * @return STUN服务器地址
	 */
	private InetSocketAddress buildServerAddress(String server) {
		LOGGER.debug("STUN服务器地址：{}", server);
		final String[] args = server.split(SymbolConfig.Symbol.COLON.toString());
		final int argLength = args.length;
		final String lastArg = args[argLength - 1];
		if(argLength == 0) {
			LOGGER.warn("STUN服务器格式错误：{}", server);
			return null;
		} else if(argLength == 1) {
			return NetUtils.buildSocketAddress(lastArg, StunConfig.DEFAULT_PORT);
		} else {
			if(StringUtils.isNumeric(lastArg)) {
				return NetUtils.buildSocketAddress(args[argLength - 2], Integer.parseInt(lastArg));
			} else {
				return NetUtils.buildSocketAddress(lastArg, StunConfig.DEFAULT_PORT);
			}
		}
	}
	
}
