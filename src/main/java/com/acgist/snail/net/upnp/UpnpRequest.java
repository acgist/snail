package com.acgist.snail.net.upnp;

import org.w3c.dom.Element;

import com.acgist.snail.net.upnp.UpnpService.Protocol;
import com.acgist.snail.utils.XMLUtils;

/**
 * UPNP请求
 */
public class UpnpRequest {

	public static final String NAMESPACE_URI = "http://schemas.xmlsoap.org/soap/envelope/";
	public static final String ENCODING_STYLE = "http://schemas.xmlsoap.org/soap/encoding/";
	
	private XMLUtils xml;
	private Element body;
	private String serviceType;
	
	public static final UpnpRequest newRequest(String serviceType) {
		UpnpRequest request = new UpnpRequest();
		request.build();
		request.serviceType = serviceType;
		return request;
	}
	
	/**
	 * 新建报文BODY
	 */
	private void build() {
		xml = XMLUtils.create();
		Element envelope = xml.elementNS(xml.document(), "s:Envelope", NAMESPACE_URI);
		envelope.setAttributeNS(NAMESPACE_URI, "encodingStyle", ENCODING_STYLE);
		body = xml.element(envelope, "s:Body");
	}

//	请求报文：
//	<?xml version="1.0"?>
//	<s:Envelope xmlns:s="http://schemas.xmlsoap.org/soap/envelope/" s:encodingStyle="http://schemas.xmlsoap.org/soap/encoding/">
//	<s:Body>
//	<u:GetExternalIPAddress xmlns:u="urn:schemas-upnp-org:service:WANIPConnection:1"></u:GetExternalIPAddress>
//	</s:Body>
//	</s:Envelope>
	/**
	 * 获取外网IP请求
	 */
	public String buildGetExternalIPAddress() {
		xml.elementNS(body, "u:GetExternalIPAddress", serviceType);
		return xml();
	}
	
//	请求报文：
//	<?xml version="1.0"?>
//	<s:Envelope xmlns:s="http://schemas.xmlsoap.org/soap/envelope/" s:encodingStyle="http://schemas.xmlsoap.org/soap/encoding/">
//		<s:Body>
//			<u:GetSpecificPortMappingEntry xmlns:u="urn:schemas-upnp-org:service:WANIPConnection:1">
//				<NewRemoteHost></NewRemoteHost>
//				<NewExternalPort>8080</NewExternalPort><!-- 外网端口 -->
//				<NewProtocol>TCP</NewProtocol>
//			</u:GetSpecificPortMappingEntry>
//		</s:Body>
//	</s:Envelope>
	/**
	 * 获取端口映射情况
	 */
	public String buildGetSpecificPortMappingEntry(int port, Protocol protocol) {
		Element mapping = xml.elementNS(body, "u:GetSpecificPortMappingEntry", serviceType);
		xml.element(mapping, "NewRemoteHost", "");
		xml.element(mapping, "NewExternalPort", String.valueOf(port));
		xml.element(mapping, "NewProtocol", protocol.name());
		return xml();
	}
	
//	请求报文：
//	<?xml version="1.0"?>
//	<s:Envelope xmlns:s="http://schemas.xmlsoap.org/soap/envelope/" s:encodingStyle="http://schemas.xmlsoap.org/soap/encoding/">
//		<s:Body>
//			<u:AddPortMapping xmlns:u="urn:schemas-upnp-org:service:WANIPConnection:1">
//				<NewRemoteHost></NewRemoteHost>
//				<NewExternalPort>8080</NewExternalPort><!-- 外网端口 -->
//				<NewProtocol>TCP</NewProtocol>
//				<NewInternalPort>8080</NewInternalPort><!-- 内网端口 -->
//				<NewInternalClient>192.168.1.100</NewInternalClient><!-- 外网地址 -->
//				<NewEnabled>1</NewEnabled>
//				<NewPortMappingDescription>JUAN UPNP</NewPortMappingDescription>
//				<NewLeaseDuration>0</NewLeaseDuration>
//			</u:AddPortMapping>
//		</s:Body>
//	</s:Envelope>
	/**
	 * 添加端口映射
	 */
	public String buildAddPortMapping(int port, String address, Protocol protocol) {
		Element mapping = xml.elementNS(body, "u:GetSpecificPortMappingEntry", serviceType);
		xml.element(mapping, "NewRemoteHost", "");
		xml.element(mapping, "NewExternalPort", String.valueOf(port));
		xml.element(mapping, "NewProtocol", protocol.name());
		xml.element(mapping, "NewInternalPort", String.valueOf(port));
		xml.element(mapping, "NewInternalClient", address);
		xml.element(mapping, "NewEnabled", "1");
		xml.element(mapping, "NewPortMappingDescription", "JUAN UPNP");
		xml.element(mapping, "NewLeaseDuration", "0");
		return xml();
	}
	
	/**
	 * XML文本输出
	 */
	private String xml() {
		return xml.xml(true);
	}
	
}
