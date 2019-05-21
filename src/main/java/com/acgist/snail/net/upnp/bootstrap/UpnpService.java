package com.acgist.snail.net.upnp.bootstrap;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.net.http.HTTPClient;
import com.acgist.snail.system.config.SystemConfig;
import com.acgist.snail.system.exception.NetException;
import com.acgist.snail.utils.CollectionUtils;
import com.acgist.snail.utils.NetUtils;
import com.acgist.snail.utils.XMLUtils;

/**
 * <p>UPNP Service</p>
 * <p>端口映射，将内网的端口映射到外网中。如果外网端口已经被映射，需要设置新的映射端口。</p>
 * TODO：多路由环境获取IP
 * 
 * @author acgist
 * @since 1.0.0
 */
public class UpnpService {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(UpnpService.class);
	
	/**
	 * 没有初始化
	 */
	public static final int USE_INIT = -2;
	/**
	 * 不可用（已被注册）
	 */
	public static final int USE_DISABLE = -1;
	/**
	 * 可用（需要注册）
	 */
	public static final int USE_MAPPING = 0;
	/**
	 * 可用（已被注册）
	 */
	public static final int USE_USEABLE = 1;
	
	/**
	 * 控制类型
	 */
	private static final String SERVICE_TYPE = "urn:schemas-upnp-org:service:WANIPConnection:1";
	
	/**
	 * 协议
	 */
	public enum Protocol {
		TCP,
		UDP;
	}
	
	private String location; // 描述文件地址
	private String controlURL; // 控制URL
	private String serviceType; // 服务类型
	
	private boolean init = false; // 是否初始化
	private String externalIpAddress; // 本地IP

	private static final UpnpService INSTANCE = new UpnpService();
	
	private UpnpService() {
	}

	public static final UpnpService getInstance() {
		return INSTANCE;
	}
	
	/**
	 * 加载信息
	 */
	public UpnpService load(String location) throws NetException {
		LOGGER.info("设置UPNP，地址：{}", location);
		this.location = location;
		var client = HTTPClient.newClient();
		var request = HTTPClient.newRequest(this.location)
			.GET()
			.build();
		var response = HTTPClient.request(client, request, BodyHandlers.ofString());
		final String body = response.body();
		final XMLUtils xml = XMLUtils.load(body);
		final List<String> serviceTypes = xml.elementValues("serviceType");
		final List<String> controlURLs = xml.elementValues("controlURL");
		if(CollectionUtils.isEmpty(serviceTypes)) {
			LOGGER.warn("加载UPNP信息失败");
			return this;
		}
		for (int index = 0; index < serviceTypes.size(); index++) {
			String serviceType = serviceTypes.get(index);
			if(SERVICE_TYPE.equals(serviceType)) {
				this.serviceType = serviceType;
				this.controlURL = controlURLs.get(index);
				this.controlURL();
				LOGGER.info("服务类型：{}", this.serviceType);
				LOGGER.info("控制地址：{}", this.controlURL);
				break;
			}
		}
		this.init = true;
		return this;
	}

	/**
	 * 外网IP地址
	 */
	public String externalIpAddress() {
		return this.externalIpAddress;
	}
	
	/**
	 * <p>获取外网IP：GetExternalIPAddress</p>
	 * <p>请求头：SOAPAction:"urn:schemas-upnp-org:service:WANIPConnection:1#GetExternalIPAddress"</p>
	 */
	public String getExternalIPAddress() throws NetException {
		if(!this.init) {
			return null;
		}
		UpnpRequest upnpRequest = UpnpRequest.newRequest(this.serviceType);
		String xml = upnpRequest.buildGetExternalIPAddress();
		var client = HTTPClient.newClient();
		var request = HTTPClient.newRequest(this.controlURL)
			.header("SOAPAction", "\"" + this.serviceType + "#GetExternalIPAddress\"")
			.POST(BodyPublishers.ofString(xml))
			.build();
		var response = HTTPClient.request(client, request, BodyHandlers.ofString());
		String body = response.body();
		return UpnpResponse.parseGetExternalIPAddress(body);
	}

	/**
	 * <p>获取端口映射情况：GetSpecificPortMappingEntry</p>
	 * <p>请求头：SOAPAction:"urn:schemas-upnp-org:service:WANIPConnection:1#GetSpecificPortMappingEntry"</p>
	 * <p>如果没有映射：返回500错误代码</p>
	 * 
	 * @return {@link #USE_INIT}、{@link #USE_DISABLE}、{@link #USE_MAPPING}、{@link #USE_USEABLE}
	 */
	public int getSpecificPortMappingEntry(int port, Protocol protocol) throws NetException {
		if(!this.init) {
			return USE_INIT;
		}
		UpnpRequest upnpRequest = UpnpRequest.newRequest(this.serviceType);
		String xml = upnpRequest.buildGetSpecificPortMappingEntry(port, protocol);
		var client = HTTPClient.newClient();
		var request = HTTPClient.newRequest(this.controlURL)
			.header("SOAPAction", "\"" + this.serviceType + "#GetSpecificPortMappingEntry\"")
			.POST(BodyPublishers.ofString(xml))
			.build();
		var response = HTTPClient.request(client, request, BodyHandlers.ofString());
		String body = response.body();
		if(response.statusCode() == 500) {
			return USE_MAPPING;
		}
		String registerIpAddress = UpnpResponse.parseGetSpecificPortMappingEntry(body);
		final String ipAddress = NetUtils.inetHostAddress();
		if(ipAddress.equals(registerIpAddress)) {
			return USE_USEABLE;
		} else {
			return USE_DISABLE;
		}
	}
	
	/**
	 * <p>添加端口映射：AddPortMapping</p>
	 * <p>请求头：SOAPAction:"urn:schemas-upnp-org:service:WANIPConnection:1#AddPortMapping"</p>
	 */
	public boolean addPortMapping(int port, int portExt, Protocol protocol) throws NetException {
		if(!this.init) {
			return false;
		}
		final String address = NetUtils.inetHostAddress();
		UpnpRequest upnpRequest = UpnpRequest.newRequest(this.serviceType);
		String xml = upnpRequest.buildAddPortMapping(port, address, portExt, protocol);
		var client = HTTPClient.newClient();
		var request = HTTPClient.newRequest(this.controlURL)
			.header("SOAPAction", "\"" + this.serviceType + "#AddPortMapping\"")
			.POST(BodyPublishers.ofString(xml))
			.build();
		var response = HTTPClient.request(client, request, BodyHandlers.ofString());
		return response.statusCode() == 200;
	}
	
	/**
	 * <p>删除端口映射：DeletePortMapping</p>
	 * <p>请求头：SOAPAction:"urn:schemas-upnp-org:service:WANIPConnection:1#DeletePortMapping"</p>
	 */
	public boolean deletePortMapping(int port, Protocol protocol) throws NetException {
		if(!this.init) {
			return false;
		}
		UpnpRequest upnpRequest = UpnpRequest.newRequest(this.serviceType);
		String xml = upnpRequest.buildDeletePortMapping(port, protocol);
		var client = HTTPClient.newClient();
		var request = HTTPClient.newRequest(this.controlURL)
			.header("SOAPAction", "\"" + this.serviceType + "#DeletePortMapping\"")
			.POST(BodyPublishers.ofString(xml))
			.build();
		var response = HTTPClient.request(client, request, BodyHandlers.ofString());
		return response.statusCode() == 200;
	}
	
	/**
	 * 设置：本机IP，端口绑定等操作
	 */
	public void setting() throws NetException {
		if(!this.init) {
			return;
		}
		setPortMapping();
		setExternalIpAddress();
	}
	
	/**
	 * 端口释放
	 */
	public void release() {
		if(!this.init) {
			return;
		}
		try {
			boolean dhtOk = this.deletePortMapping(SystemConfig.getServicePortExt(), Protocol.UDP);
			boolean peerOk = this.deletePortMapping(SystemConfig.getServicePortExt(), Protocol.TCP);
			LOGGER.info("端口释放：DHT：{}、Peer：{}", dhtOk, peerOk);
		} catch (NetException e) {
			LOGGER.error("释放UPNP端口异常", e);
		}
	}
	
	/**
	 * 设置控制地址
	 */
	private void controlURL() {
		URL url = null;
		try {
			url = new URL(this.location);
		} catch (MalformedURLException e) {
			LOGGER.error("UPNP获取控制URL异常：{}", this.location, e);
		}
		final StringBuilder builder = new StringBuilder();
		builder.append(url.getProtocol())
			.append("://")
			.append(url.getAuthority())
			.append(this.controlURL);
		this.controlURL = builder.toString();
	}
	
	/**
	 * 端口映射，如果端口被占用，端口+1继续映射。
	 */
	private void setPortMapping() throws NetException {
		int uValue, tValue;
		int portExt = SystemConfig.getServicePort();
		while(true) {
			uValue = this.getSpecificPortMappingEntry(portExt, Protocol.UDP);
			if(uValue == USE_INIT || uValue == USE_DISABLE) {
				portExt++;
				continue;
			}
			tValue = this.getSpecificPortMappingEntry(portExt, Protocol.TCP);
			if(uValue == tValue) {
				break;
			} else {
				portExt++;
			}
		}
		if(uValue == USE_MAPPING) {
			SystemConfig.setServicePortExt(portExt);
			boolean dhtOk = this.addPortMapping(SystemConfig.getServicePort(), portExt, Protocol.UDP);
			boolean peerOk = this.addPortMapping(SystemConfig.getServicePort(), portExt, Protocol.TCP);
			LOGGER.info("端口映射：DHT（{}-{}-{}）、Peer（{}-{}-{}）", SystemConfig.getServicePort(), portExt, dhtOk, SystemConfig.getServicePort(), portExt, peerOk);
		} else if(tValue == USE_USEABLE) {
			SystemConfig.setServicePortExt(portExt);
			LOGGER.info("端口映射：DHT（{}-{}-{}）、Peer（{}-{}-{}）", SystemConfig.getServicePort(), portExt, true, SystemConfig.getServicePort(), portExt, true);
		} else {
			LOGGER.error("端口映射失败");
		}
	}
	
	/**
	 * 外网IP地址
	 */
	private void setExternalIpAddress() throws NetException {
		final String externalIpAddress = this.getExternalIPAddress();
		LOGGER.info("外网IP地址：{}", externalIpAddress);
		if(this.externalIpAddress == null) {
			this.externalIpAddress = externalIpAddress;
		} else if(!this.externalIpAddress.equals(externalIpAddress)) {
			this.externalIpAddress = externalIpAddress;
			this.change();
		}
	}
	
	/**
	 * IP地址发生变化
	 */
	private void change() {
		LOGGER.info("外网IP地址发生变化");
	}

}
