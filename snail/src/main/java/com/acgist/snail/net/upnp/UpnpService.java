package com.acgist.snail.net.upnp;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.config.SystemConfig;
import com.acgist.snail.context.exception.NetException;
import com.acgist.snail.format.XML;
import com.acgist.snail.net.http.HttpClient;
import com.acgist.snail.net.http.HttpClient.StatusCode;
import com.acgist.snail.pojo.wrapper.URIWrapper;
import com.acgist.snail.protocol.Protocol;
import com.acgist.snail.utils.CollectionUtils;
import com.acgist.snail.utils.NetUtils;
import com.acgist.snail.utils.StringUtils;
import com.acgist.snail.utils.UrlUtils;

/**
 * <p>UPNP Service</p>
 * <p>Internet Gateway Device</p>
 * <p>端口映射：将内网的端口映射到外网中</p>
 * <p>如果外网端口已经被映射：设置新的映射端口</p>
 * <p>注：多路由环境使用STUN进行内网穿透</p>
 * 
 * @author acgist
 */
public final class UpnpService {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(UpnpService.class);
	
	private static final UpnpService INSTANCE = new UpnpService();
	
	public static final UpnpService getInstance() {
		return INSTANCE;
	}
	
	/**
	 * <p>UPNP映射状态</p>
	 */
	public enum Status {
		
		/** 未初始化 */
		UNINIT,
		/** 不可用：已被注册 */
		DISABLE,
		/** 可用：需要注册 */
		MAPABLE,
		/** 可用：已被注册 */
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
	 * <p>端口是否已经映射</p>
	 */
	private volatile boolean useable = false;
	/**
	 * <p>是否可用</p>
	 * <p>控制连接是否已经设置</p>
	 */
	private volatile boolean available = false;
	/**
	 * <p>是否需要重新映射</p>
	 */
	private volatile boolean remapping = false;

	private UpnpService() {
	}
	
	/**
	 * <p>加载信息</p>
	 * 
	 * @param location 描述文件地址
	 * 
	 * @return UpnpService
	 * 
	 * @throws NetException 网络异常
	 */
	public UpnpService load(String location) throws NetException {
		final URIWrapper wrapper = URIWrapper.newInstance(location).decode();
		if(!NetUtils.gateway(wrapper.host())) {
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
		final List<String> controlUrls = xml.elementValues("controlURL");
		if(CollectionUtils.isEmpty(serviceTypes)) {
			LOGGER.warn("UPNP设置失败（服务类型）：{}", body);
			return this;
		}
		boolean success = false;
		for (int index = 0; index < serviceTypes.size(); index++) {
			final String serviceType = serviceTypes.get(index);
			// 控制地址
			if(StringUtils.startsWith(serviceType, SERVICE_WANIPC)) {
				success = true;
				this.available = true;
				this.remapping = true;
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
	 * <p>判断是否可用</p>
	 * 
	 * @return 是否可用
	 */
	public boolean useable() {
		return this.useable;
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
	 * <p>如果没有映射：返回{@linkplain StatusCode#INTERNAL_SERVER_ERROR 500}状态码</p>
	 * 
	 * @param portExt 外网端口
	 * @param protocol 协议
	 * 
	 * @return {@linkplain Status 状态}
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
		if(client.internalServerError()) {
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
	 * 
	 * @throws NetException 网络异常
	 */
	public void mapping() throws NetException {
		if(!this.available) {
			return;
		}
		if(this.remapping) {
			this.remapping = false;
			final String externalIpAddress = this.getExternalIPAddress();
			if(NetUtils.localIPAddress(externalIpAddress)) {
				// 获取的公网IP地址为内网地址：多重路由环境
				LOGGER.warn("UPNP端口映射失败：多重路由环境");
			} else {
				SystemConfig.setExternalIpAddress(externalIpAddress);
				this.addMapping();
			}
		}
	}
	
	/**
	 * <p>端口释放</p>
	 */
	public void release() {
		if(this.useable && this.available) {
			try {
				final boolean udpOk = this.deletePortMapping(SystemConfig.getTorrentPortExt(), Protocol.Type.UDP);
				final boolean tcpOk = this.deletePortMapping(SystemConfig.getTorrentPortExt(), Protocol.Type.TCP);
				LOGGER.debug("释放UPNP端口：UDP：{}、TCP：{}", udpOk, tcpOk);
			} catch (NetException e) {
				LOGGER.error("释放UPNP端口异常", e);
			}
			// 必须释放端口后才能修改状态
			this.useable = false;
			this.available = false;
		}
	}
	
	/**
	 * <p>端口映射</p>
	 * <p>如果端口被占用：端口{@code +1}继续映射</p>
	 * 
	 * @throws NetException 网络异常
	 */
	private void addMapping() throws NetException {
		Status udpStatus = Status.DISABLE, tcpStatus;
		int portExt = SystemConfig.getTorrentPort(); // 外网端口
		while(true) {
			if(portExt >= NetUtils.MAX_PORT) {
				break;
			}
			// UDP
			udpStatus = this.getSpecificPortMappingEntry(portExt, Protocol.Type.UDP);
			if(udpStatus == Status.UNINIT || udpStatus == Status.DISABLE) {
				portExt++;
				continue;
			}
			// TCP
			tcpStatus = this.getSpecificPortMappingEntry(portExt, Protocol.Type.TCP);
			if(udpStatus == tcpStatus) {
				break;
			} else {
				portExt++;
			}
		}
		if(udpStatus == Status.MAPABLE) {
			this.useable = true;
			SystemConfig.setTorrentPortExt(portExt);
			final boolean udpOk = this.addPortMapping(SystemConfig.getTorrentPort(), portExt, Protocol.Type.UDP);
			final boolean tcpOk = this.addPortMapping(SystemConfig.getTorrentPort(), portExt, Protocol.Type.TCP);
			LOGGER.debug("UPNP端口映射（注册）：UDP（{}-{}-{}）、TCP（{}-{}-{}）", SystemConfig.getTorrentPort(), portExt, udpOk, SystemConfig.getTorrentPort(), portExt, tcpOk);
		} else if(udpStatus == Status.USEABLE) {
			this.useable = true;
			SystemConfig.setTorrentPortExt(portExt);
			LOGGER.debug("UPNP端口映射（可用）：UDP（{}-{}-{}）、TCP（{}-{}-{}）", SystemConfig.getTorrentPort(), portExt, true, SystemConfig.getTorrentPort(), portExt, true);
		} else {
			this.useable = false;
			LOGGER.warn("UPNP端口映射失败");
		}
	}
	
}
