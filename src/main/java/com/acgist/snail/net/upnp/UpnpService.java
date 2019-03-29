package com.acgist.snail.net.upnp;

import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;

import com.acgist.snail.net.http.HTTPClient;

/**
 * UPNP操作
 */
public class UpnpService {
	
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

	/**
	 * 加载信息
	 */
	public void load() {
		var client = HTTPClient.newClient();
		var request = HTTPClient.newRequest(location)
			.GET()
			.build();
		var response = HTTPClient.request(client, request, BodyHandlers.ofString());
		final String body = response.body();
	}

	/**
	 * 获取外网IP：GetExternalIPAddress
	 * 请求头：SOAPAction:"urn:schemas-upnp-org:service:WANIPConnection:1#GetExternalIPAddress"
	 */
	public String externalIPAddress() {
		UpnpRequest upnpRequest = UpnpRequest.newRequest(serviceType);
		String xml = upnpRequest.buildGetExternalIPAddress();
		var client = HTTPClient.newClient();
		var request = HTTPClient.newRequest(controlURL)
			.header("SOAPAction", "\"" + serviceType + "#GetExternalIPAddress\"")
			.POST(BodyPublishers.ofString(xml))
			.build();
		var response = HTTPClient.request(client, request, BodyHandlers.ofString());
		String body = response.body();
		return UpnpResponse.parseGetExternalIPAddress(body);
	}

	/**
	 * 获取端口映射情况：GetSpecificPortMappingEntry
	 * 请求头：SOAPAction:"urn:schemas-upnp-org:service:WANIPConnection:1#GetSpecificPortMappingEntry"
	 * 如果没有映射：返回500错误代码
	 */
	public boolean specificPortMappingEntry(int port, Protocol protocol) {
		UpnpRequest upnpRequest = UpnpRequest.newRequest(serviceType);
		String xml = upnpRequest.buildGetSpecificPortMappingEntry(port, protocol);
		var client = HTTPClient.newClient();
		var request = HTTPClient.newRequest(controlURL)
			.header("SOAPAction", "\"" + serviceType + "#GetSpecificPortMappingEntry\"")
			.POST(BodyPublishers.ofString(xml))
			.build();
		var response = HTTPClient.request(client, request, BodyHandlers.ofString());
		return response.statusCode() == 500; // 没被使用
	}
	
	/**
	 * 添加端口映射：AddPortMapping
	 * 请求头：SOAPAction:"urn:schemas-upnp-org:service:WANIPConnection:1#AddPortMapping"
	 */
	public boolean addPortMapping(int port, String address, Protocol protocol) {
		UpnpRequest upnpRequest = UpnpRequest.newRequest(serviceType);
		String xml = upnpRequest.buildAddPortMapping(port, address, protocol);
		var client = HTTPClient.newClient();
		var request = HTTPClient.newRequest(controlURL)
			.header("SOAPAction", "\"" + serviceType + "#AddPortMapping\"")
			.POST(BodyPublishers.ofString(xml))
			.build();
		var response = HTTPClient.request(client, request, BodyHandlers.ofString());
		return response.statusCode() == 200;
	}
	
}
