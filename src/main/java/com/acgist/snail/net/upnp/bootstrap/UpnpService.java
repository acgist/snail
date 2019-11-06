package com.acgist.snail.net.upnp.bootstrap;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.net.http.HTTPClient;
import com.acgist.snail.protocol.Protocol;
import com.acgist.snail.system.config.SystemConfig;
import com.acgist.snail.system.context.NatContext;
import com.acgist.snail.system.exception.NetException;
import com.acgist.snail.utils.CollectionUtils;
import com.acgist.snail.utils.NetUtils;
import com.acgist.snail.utils.StringUtils;
import com.acgist.snail.utils.XMLUtils;

/**
 * <p>UPNP Service</p>
 * <p>Internet Gateway Device</p>
 * <p>端口映射，将内网的端口映射到外网中。如果外网端口已经被映射，需要设置新的映射端口。</p>
 * <p>注：多路由环境使用STUN进行内网穿透</p>
 * 
 * @author acgist
 * @since 1.0.0
 */
public class UpnpService {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(UpnpService.class);
	
	private static final UpnpService INSTANCE = new UpnpService();
	
	/**
	 * UPNP映射状态
	 */
	public enum Status {
		
		/** 未初始化 */
		UNINIT,
		/** 不可用（已被注册） */
		DISABLE,
		/** 可用（需要注册） */
		MAPABLE,
		/** 可用（已被注册） */
		USEABLE;
		
	}
	
	/**
	 * 控制类型，最后一位类型忽略。
	 */
	private static final String SERVICE_WANIPC = "urn:schemas-upnp-org:service:WANIPConnection:";
	
	/**
	 * 描述文件地址
	 */
	private String location;
	/**
	 * 控制地址
	 */
	private String controlUrl;
	/**
	 * 服务类型
	 */
	private String serviceType;
	/**
	 * 可用状态
	 */
	private volatile boolean available = false;
	/**
	 * 映射状态
	 */
	private volatile boolean useable = false;

	private UpnpService() {
	}

	public static final UpnpService getInstance() {
		return INSTANCE;
	}
	
	/**
	 * 加载信息
	 */
	public UpnpService load(String location) throws NetException {
		LOGGER.info("UPNP设置描述文件地址：{}", location);
		this.location = location;
		final var response = HTTPClient.get(this.location, BodyHandlers.ofString());
		final var body = response.body();
		final var xml = XMLUtils.load(body);
		// 服务类型和服务地址
		final List<String> serviceTypes = xml.elementValues("serviceType");
		final List<String> controlUrls = xml.elementValues("controlURL");
		if(CollectionUtils.isEmpty(serviceTypes)) {
			LOGGER.warn("UPNP设置失败（服务类型）：{}", serviceTypes);
			return this;
		}
		for (int index = 0; index < serviceTypes.size(); index++) {
			final String serviceType = serviceTypes.get(index);
			if(StringUtils.startsWith(serviceType, SERVICE_WANIPC)) {
				this.serviceType = serviceType;
				this.controlUrl = controlUrls.get(index);
				this.controlUrl();
				LOGGER.info("UPNP服务类型：{}", this.serviceType);
				LOGGER.info("UPNP控制地址：{}", this.controlUrl);
				break;
			}
		}
		this.available = true;
		return this;
	}

	/**
	 * 是否可用
	 */
	public boolean useable() {
		return this.useable;
	}
	
	/**
	 * <p>外网IP地址：GetExternalIPAddress</p>
	 * <p>请求头：SOAPAction:"urn:schemas-upnp-org:service:WANIPConnection:1#GetExternalIPAddress"</p>
	 */
	public String getExternalIPAddress() throws NetException {
		if(!this.available) {
			return null;
		}
		final var upnpRequest = UpnpRequest.newRequest(this.serviceType);
		final var xml = upnpRequest.buildGetExternalIPAddress();
		final var client = HTTPClient.newInstance(this.controlUrl);
		final var response = client
			.header("SOAPAction", "\"" + this.serviceType + "#GetExternalIPAddress\"")
			.post(xml, BodyHandlers.ofString());
		final var body = response.body();
		return UpnpResponse.parseGetExternalIPAddress(body);
	}

	/**
	 * <p>端口映射信息：GetSpecificPortMappingEntry</p>
	 * <p>请求头：SOAPAction:"urn:schemas-upnp-org:service:WANIPConnection:1#GetSpecificPortMappingEntry"</p>
	 * <p>如果没有映射：返回{@link HTTPClient#HTTP_INTERNAL_SERVER_ERROR}错误状态码</p>
	 * 
	 * @return {@linkplain Status 状态}
	 */
	public Status getSpecificPortMappingEntry(int portExt, Protocol.Type protocol) throws NetException {
		if(!this.available) {
			return Status.UNINIT;
		}
		final var upnpRequest = UpnpRequest.newRequest(this.serviceType);
		final var xml = upnpRequest.buildGetSpecificPortMappingEntry(portExt, protocol);
		final var client = HTTPClient.newInstance(this.controlUrl);
		final var response = client
			.header("SOAPAction", "\"" + this.serviceType + "#GetSpecificPortMappingEntry\"")
			.post(xml, BodyHandlers.ofString());
		final var body = response.body();
		if(HTTPClient.internalServerError(response)) {
			return Status.MAPABLE;
		}
		final var registerIp = UpnpResponse.parseGetSpecificPortMappingEntry(body);
		final var localIp = NetUtils.localHostAddress();
		if(localIp.equals(registerIp)) {
			return Status.USEABLE;
		} else {
			return Status.DISABLE;
		}
	}
	
	/**
	 * <p>添加端口映射：AddPortMapping</p>
	 * <p>请求头：SOAPAction:"urn:schemas-upnp-org:service:WANIPConnection:1#AddPortMapping"</p>
	 */
	public boolean addPortMapping(int port, int portExt, Protocol.Type protocol) throws NetException {
		if(!this.available) {
			return false;
		}
		final var address = NetUtils.localHostAddress();
		final var upnpRequest = UpnpRequest.newRequest(this.serviceType);
		final var xml = upnpRequest.buildAddPortMapping(port, address, portExt, protocol);
		final var client = HTTPClient.newInstance(this.controlUrl);
		final var response = client
			.header("SOAPAction", "\"" + this.serviceType + "#AddPortMapping\"")
			.post(xml, BodyHandlers.ofString());
		return HTTPClient.ok(response);
	}
	
	/**
	 * <p>删除端口映射：DeletePortMapping</p>
	 * <p>请求头：SOAPAction:"urn:schemas-upnp-org:service:WANIPConnection:1#DeletePortMapping"</p>
	 */
	public boolean deletePortMapping(int portExt, Protocol.Type protocol) throws NetException {
		if(!this.available) {
			return false;
		}
		final var upnpRequest = UpnpRequest.newRequest(this.serviceType);
		final var xml = upnpRequest.buildDeletePortMapping(portExt, protocol);
		final var client = HTTPClient.newInstance(this.controlUrl);
		final var response = client
			.header("SOAPAction", "\"" + this.serviceType + "#DeletePortMapping\"")
			.post(xml, BodyHandlers.ofString());
		return HTTPClient.ok(response);
	}
	
	/**
	 * 映射端口：映射端口、获取本机外网IP地址。
	 */
	public void mapping() throws NetException {
		if(!this.available) {
			return;
		}
		final String externalIpAddress = this.getExternalIPAddress();
		if(NetUtils.isLocalIp(externalIpAddress)) {
			LOGGER.warn("UPNP端口映射获取外网IP地址为本地地址");
		} else {
			SystemConfig.setExternalIpAddress(externalIpAddress);
			setPortMapping();
		}
		NatContext.getInstance().unlock();
	}
	
	/**
	 * 端口释放
	 */
	public void release() {
		if(this.useable && this.available) {
			this.useable = false;
			this.available = false;
			try {
				final boolean udpOk = this.deletePortMapping(SystemConfig.getTorrentPortExt(), Protocol.Type.UDP);
				final boolean tcpOk = this.deletePortMapping(SystemConfig.getTorrentPortExt(), Protocol.Type.TCP);
				LOGGER.info("释放UPNP端口：UDP：{}、TCP：{}", udpOk, tcpOk);
			} catch (NetException e) {
				LOGGER.error("释放UPNP端口异常", e);
			}
		}
	}
	
	/**
	 * 设置控制地址
	 */
	private void controlUrl() throws NetException {
		URL url = null;
		try {
			url = new URL(this.location);
		} catch (MalformedURLException e) {
			throw new NetException("UPNP端口映射获取描述文件地址异常：" + this.location, e);
		}
		final StringBuilder builder = new StringBuilder();
		builder.append(url.getProtocol())
			.append("://")
			.append(url.getAuthority())
			.append(this.controlUrl);
		this.controlUrl = builder.toString();
	}
	
	/**
	 * <p>端口映射</p>
	 * <p>如果端口被占用，则端口+1继续映射。</p>
	 */
	private void setPortMapping() throws NetException {
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
			LOGGER.info("UPNP端口映射（注册）：UDP（{}-{}-{}）、TCP（{}-{}-{}）", SystemConfig.getTorrentPort(), portExt, udpOk, SystemConfig.getTorrentPort(), portExt, tcpOk);
		} else if(udpStatus == Status.USEABLE) {
			this.useable = true;
			SystemConfig.setTorrentPortExt(portExt);
			LOGGER.info("UPNP端口映射（可用）：UDP（{}-{}-{}）、TCP（{}-{}-{}）", SystemConfig.getTorrentPort(), portExt, true, SystemConfig.getTorrentPort(), portExt, true);
		} else {
			this.useable = false;
			LOGGER.warn("UPNP端口映射失败");
		}
	}
	
}
