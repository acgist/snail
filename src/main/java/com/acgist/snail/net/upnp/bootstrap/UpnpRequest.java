package com.acgist.snail.net.upnp.bootstrap;

import org.w3c.dom.Element;

import com.acgist.snail.system.config.Protocol;
import com.acgist.snail.utils.XMLUtils;

/**
 * UPNP请求
 * 
 * @author acgist
 * @since 1.0.0
 */
public class UpnpRequest {

	private static final String NAMESPACE_URI = "http://schemas.xmlsoap.org/soap/envelope/";
	private static final String ENCODING_STYLE = "http://schemas.xmlsoap.org/soap/encoding/";
	
	private XMLUtils xml; // XML工具
	private Element body; // 主要内容
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
		this.xml = XMLUtils.create();
		Element envelope = this.xml.elementNS(xml.document(), "s:Envelope", NAMESPACE_URI);
		envelope.setAttributeNS(NAMESPACE_URI, "encodingStyle", ENCODING_STYLE);
		this.body = this.xml.element(envelope, "s:Body");
	}

//	请求报文：
//	<?xml version="1.0"?>
//	<s:Envelope xmlns:s="http://schemas.xmlsoap.org/soap/envelope/" s:encodingStyle="http://schemas.xmlsoap.org/soap/encoding/">
//		<s:Body>
//			<u:GetExternalIPAddress xmlns:u="urn:schemas-upnp-org:service:WANIPConnection:1"></u:GetExternalIPAddress>
//		</s:Body>
//	</s:Envelope>
	/**
	 * 获取外网IP请求
	 */
	public String buildGetExternalIPAddress() {
		this.xml.elementNS(this.body, "u:GetExternalIPAddress", this.serviceType);
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
		Element mapping = this.xml.elementNS(this.body, "u:GetSpecificPortMappingEntry", this.serviceType);
		this.xml.element(mapping, "NewRemoteHost", "");
		this.xml.element(mapping, "NewExternalPort", String.valueOf(port));
		this.xml.element(mapping, "NewProtocol", protocol.name().toUpperCase());
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
//				<NewPortMappingDescription>描述</NewPortMappingDescription>
//				<NewLeaseDuration>0</NewLeaseDuration>
//			</u:AddPortMapping>
//		</s:Body>
//	</s:Envelope>
	/**
	 * 添加端口映射
	 * 
	 * @param port 内网端口
	 * @param address 内网地址
	 * @param portExt 外网端口
	 */
	public String buildAddPortMapping(int port, String address, int portExt, Protocol protocol) {
		Element mapping = this.xml.elementNS(this.body, "u:AddPortMapping", this.serviceType);
		this.xml.element(mapping, "NewRemoteHost", "");
		this.xml.element(mapping, "NewExternalPort", String.valueOf(portExt));
		this.xml.element(mapping, "NewProtocol", protocol.name().toUpperCase());
		this.xml.element(mapping, "NewInternalPort", String.valueOf(port));
		this.xml.element(mapping, "NewInternalClient", address);
		this.xml.element(mapping, "NewEnabled", "1");
		this.xml.element(mapping, "NewPortMappingDescription", "Snail");
		this.xml.element(mapping, "NewLeaseDuration", "0");
		return xml();
	}
	
	/**
	 * 删除端口映射
	 */
	public String buildDeletePortMapping(int port, Protocol protocol) {
		Element mapping = this.xml.elementNS(body, "u:DeletePortMapping", this.serviceType);
		this.xml.element(mapping, "NewRemoteHost", "");
		this.xml.element(mapping, "NewExternalPort", String.valueOf(port));
		this.xml.element(mapping, "NewProtocol", protocol.name().toUpperCase());
		return xml();
	}

	/**
	 * XML文本输出
	 */
	private String xml() {
		return this.xml.xml(true);
	}

}
