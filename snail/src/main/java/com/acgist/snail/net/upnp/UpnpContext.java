package com.acgist.snail.net.upnp;

import java.util.List;

import com.acgist.snail.config.SystemConfig;
import com.acgist.snail.context.IContext;
import com.acgist.snail.context.wrapper.URIWrapper;
import com.acgist.snail.format.XML;
import com.acgist.snail.logger.Logger;
import com.acgist.snail.logger.LoggerFactory;
import com.acgist.snail.net.NetException;
import com.acgist.snail.net.http.HttpClient;
import com.acgist.snail.net.torrent.dht.NodeContext;
import com.acgist.snail.protocol.Protocol;
import com.acgist.snail.utils.CollectionUtils;
import com.acgist.snail.utils.NetUtils;
import com.acgist.snail.utils.StringUtils;
import com.acgist.snail.utils.UrlUtils;

/**
 * <p>UPNP上下文</p>
 * <p>Internet Gateway Device</p>
 * <p>端口映射：将内网的端口映射到外网中</p>
 * 
 * @author acgist
 */
public final class UpnpContext implements IContext {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(UpnpContext.class);
	
	private static final UpnpContext INSTANCE = new UpnpContext();
	
	public static final UpnpContext getInstance() {
		return INSTANCE;
	}
	
	/**
	 * <p>UPNP映射状态</p>
	 * 
	 * @author acgist
	 */
	public enum Status {
		
		/**
		 * <p>未初始化</p>
		 */
		UNINIT,
		/**
		 * <p>不可用：已被注册</p>
		 */
		DISABLE,
		/**
		 * <p>可用：需要注册</p>
		 */
		MAPABLE,
		/**
		 * <p>可用：已被注册</p>
		 */
		USEABLE;
		
	}
	
	/**
	 * <p>控制类型：最后一位类型忽略</p>
	 */
	private static final String SERVICE_WANIPC = "urn:schemas-upnp-org:service:WANIPConnection:";
	
	/**
	 * <p>描述文件地址</p>
	 */
	private String location;
	/**
	 * <p>控制地址</p>
	 */
	private String controlUrl;
	/**
	 * <p>服务类型</p>
	 */
	private String serviceType;
	/**
	 * <p>是否可用</p>
	 * <p>控制连接是否已经设置</p>
	 */
	private volatile boolean available = false;

	private UpnpContext() {
	}
	
	/**
	 * <p>判断是否可用</p>
	 * 
	 * @return 是否可用
	 */
	public boolean available() {
		return this.available;
	}
	
	/**
	 * <p>加载信息</p>
	 * 
	 * @param location 描述文件地址
	 * 
	 * @return {@link UpnpContext}
	 * 
	 * @throws NetException 网络异常
	 */
	public UpnpContext load(String location) throws NetException {
		final URIWrapper wrapper = URIWrapper.newInstance(location).decode();
		if(!NetUtils.lan(wrapper.host())) {
			// 判断处于同一内网
			LOGGER.info("UPNP描述文件错误：{}", location);
			return this;
		}
		final var body = HttpClient
			.newInstance(location)
			.get()
			.responseToString();
		final var xml = XML.load(body);
		// 服务类型和服务地址
		final List<String> serviceTypes = xml.elementValues("serviceType");
		if(CollectionUtils.isEmpty(serviceTypes)) {
			LOGGER.warn("UPNP设置失败（服务类型）：{}", body);
			return this;
		}
		boolean success = false;
		final List<String> controlUrls = xml.elementValues("controlURL");
		for (int index = 0; index < serviceTypes.size(); index++) {
			final String serviceType = serviceTypes.get(index);
			// 控制地址
			if(StringUtils.startsWith(serviceType, SERVICE_WANIPC)) {
				success = true;
				this.available = true;
				this.location = location;
				this.serviceType = serviceType;
				this.controlUrl = UrlUtils.redirect(this.location, controlUrls.get(index));
				LOGGER.debug("UPNP描述文件：{}", this.location);
				LOGGER.debug("UPNP服务类型：{}", this.serviceType);
				LOGGER.debug("UPNP控制地址：{}", this.controlUrl);
				break;
			}
		}
		if(!success) {
			LOGGER.info("UPNP描述文件无效：{}", location);
		}
		return this;
	}
	
	/**
	 * <p>外网IP地址：GetExternalIPAddress</p>
	 * <p>请求头：SOAPAction:"urn:schemas-upnp-org:service:WANIPConnection:1#GetExternalIPAddress"</p>
	 * 
	 * @return 外网IP地址
	 * 
	 * @throws NetException 网络异常
	 */
	public String getExternalIPAddress() throws NetException {
		if(!this.available) {
			return null;
		}
		final var upnpRequest = UpnpRequest.newRequest(this.serviceType);
		final var xml = upnpRequest.buildGetExternalIPAddress();
		final var body = HttpClient
			.newInstance(this.controlUrl)
			.header("SOAPAction", "\"" + this.serviceType + "#GetExternalIPAddress\"")
			.post(xml)
			.responseToString();
		return UpnpResponse.parseGetExternalIPAddress(body);
	}

	/**
	 * <p>端口映射信息：GetSpecificPortMappingEntry</p>
	 * <p>请求头：SOAPAction:"urn:schemas-upnp-org:service:WANIPConnection:1#GetSpecificPortMappingEntry"</p>
	 * 
	 * @param portExt 外网端口
	 * @param protocol 协议
	 * 
	 * @return {@link Status}
	 * 
	 * @throws NetException 网络异常
	 */
	public Status getSpecificPortMappingEntry(int portExt, Protocol.Type protocol) throws NetException {
		if(!this.available) {
			return Status.UNINIT;
		}
		final var upnpRequest = UpnpRequest.newRequest(this.serviceType);
		final var xml = upnpRequest.buildGetSpecificPortMappingEntry(portExt, protocol);
		final var client = HttpClient
			.newInstance(this.controlUrl)
			.header("SOAPAction", "\"" + this.serviceType + "#GetSpecificPortMappingEntry\"")
			.post(xml);
		if(client.internalError()) {
			return Status.MAPABLE;
		}
		final var body = client.responseToString();
		final var mappingIP = UpnpResponse.parseGetSpecificPortMappingEntry(body);
		if(NetUtils.LOCAL_HOST_ADDRESS.equals(mappingIP)) {
			return Status.USEABLE;
		} else {
			LOGGER.debug("UPNP端口已被映射：{}-{}", mappingIP, portExt);
			return Status.DISABLE;
		}
	}
	
	/**
	 * <p>添加端口映射：AddPortMapping</p>
	 * <p>请求头：SOAPAction:"urn:schemas-upnp-org:service:WANIPConnection:1#AddPortMapping"</p>
	 * 
	 * @param port 内网端口
	 * @param portExt 外网端口
	 * @param protocol 协议
	 * 
	 * @return 是否成功
	 * 
	 * @throws NetException 网络异常
	 */
	public boolean addPortMapping(int port, int portExt, Protocol.Type protocol) throws NetException {
		if(!this.available) {
			return false;
		}
		final var upnpRequest = UpnpRequest.newRequest(this.serviceType);
		final var xml = upnpRequest.buildAddPortMapping(port, NetUtils.LOCAL_HOST_ADDRESS, portExt, protocol);
		return HttpClient
			.newInstance(this.controlUrl)
			.header("SOAPAction", "\"" + this.serviceType + "#AddPortMapping\"")
			.post(xml)
			.ok();
	}
	
	/**
	 * <p>删除端口映射：DeletePortMapping</p>
	 * <p>请求头：SOAPAction:"urn:schemas-upnp-org:service:WANIPConnection:1#DeletePortMapping"</p>
	 * 
	 * @param portExt 外网端口
	 * @param protocol 协议
	 * 
	 * @return 是否成功
	 * 
	 * @throws NetException 网络异常
	 */
	public boolean deletePortMapping(int portExt, Protocol.Type protocol) throws NetException {
		if(!this.available) {
			return false;
		}
		final var upnpRequest = UpnpRequest.newRequest(this.serviceType);
		final var xml = upnpRequest.buildDeletePortMapping(portExt, protocol);
		return HttpClient
			.newInstance(this.controlUrl)
			.header("SOAPAction", "\"" + this.serviceType + "#DeletePortMapping\"")
			.post(xml)
			.ok();
	}
	
	/**
	 * <p>映射端口</p>
	 * <p>如果端口已经占用：端口递增继续映射</p>
	 * 
	 * @return 是否映射成功
	 */
	public boolean mapping() throws NetException {
		if(!this.available) {
			return false;
		}
		final String externalIPAddress = this.getExternalIPAddress();
		if(NetUtils.localIP(externalIPAddress)) {
			// 获取的公网IP地址为内网地址
			LOGGER.warn("UPNP端口映射失败（多重路由环境）：{}", externalIPAddress);
			return false;
		}
		SystemConfig.setExternalIPAddress(externalIPAddress);
		NodeContext.getInstance().buildNodeId(externalIPAddress);
		// 循环映射端口
		Status tcpStatus;
		Status udpStatus = Status.DISABLE;
		final int torrentPort = SystemConfig.getTorrentPort();
		int portExt = torrentPort;
		while(portExt < NetUtils.MAX_PORT) {
			// UDP
			udpStatus = this.getSpecificPortMappingEntry(portExt, Protocol.Type.UDP);
			if(udpStatus == Status.DISABLE) {
				portExt++;
				continue;
			}
			// TCP
			tcpStatus = this.getSpecificPortMappingEntry(portExt, Protocol.Type.TCP);
			if(udpStatus == tcpStatus) {
				break;
			} else {
				this.deletePortMapping(portExt, Protocol.Type.UDP);
				portExt++;
			}
		}
		if(udpStatus == Status.MAPABLE) {
			SystemConfig.setTorrentPortExt(portExt);
			final boolean udpOk = this.addPortMapping(torrentPort, portExt, Protocol.Type.UDP);
			final boolean tcpOk = this.addPortMapping(torrentPort, portExt, Protocol.Type.TCP);
			LOGGER.debug("UPNP端口映射（注册）：UDP（{}-{}-{}）、TCP（{}-{}-{}）", torrentPort, portExt, udpOk, torrentPort, portExt, tcpOk);
			return true;
		} else if(udpStatus == Status.USEABLE) {
			SystemConfig.setTorrentPortExt(portExt);
			LOGGER.debug("UPNP端口映射（可用）：UDP（{}-{}）、TCP（{}-{}）", torrentPort, portExt, torrentPort, portExt);
			return true;
		} else {
			LOGGER.warn("UPNP端口映射失败");
			return false;
		}
	}
	
	/**
	 * <p>端口释放</p>
	 */
	public void release() {
		if(this.available) {
			try {
				final boolean udpOk = this.deletePortMapping(SystemConfig.getTorrentPortExt(), Protocol.Type.UDP);
				final boolean tcpOk = this.deletePortMapping(SystemConfig.getTorrentPortExt(), Protocol.Type.TCP);
				LOGGER.debug("释放UPNP端口：UDP：{}、TCP：{}", udpOk, tcpOk);
			} catch (NetException e) {
				LOGGER.error("释放UPNP端口异常", e);
			}
			// 必须释放端口才能修改状态
			this.available = false;
		}
	}
	
}
