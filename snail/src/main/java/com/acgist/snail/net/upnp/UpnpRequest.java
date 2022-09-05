package com.acgist.snail.net.upnp;

import org.w3c.dom.Element;

import com.acgist.snail.config.SystemConfig;
import com.acgist.snail.format.XML;
import com.acgist.snail.protocol.Protocol;

/**
 * <p>UPNP请求</p>
 * 
 * @author acgist
 */
public final class UpnpRequest {

	/**
	 * <p>SOAP协议：{@value}</p>
	 */
	private static final String NAMESPACE_ENVELOPE = "http://schemas.xmlsoap.org/soap/envelope/";
	/**
	 * <p>SOAP协议：{@value}</p>
	 */
	private static final String NAMESPANCE_ENCODING = "http://schemas.xmlsoap.org/soap/encoding/";
	
	/**
	 * <p>XML工具</p>
	 */
	private XML xml;
	/**
	 * <p>主体节点</p>
	 */
	private Element body;
	/**
	 * <p>服务类型</p>
	 */
	private final String serviceType;
	
	/**
	 * @param serviceType 服务类型
	 */
	private UpnpRequest(String serviceType) {
		this.serviceType = serviceType;
	}
	
	/**
	 * <p>新建请求</p>
	 * 
	 * @param serviceType 服务类型
	 * 
	 * @return 请求
	 */
	public static final UpnpRequest newRequest(String serviceType) {
		final UpnpRequest request = new UpnpRequest(serviceType);
		request.build();
		return request;
	}
	
	/**
	 * <p>新建报文</p>
	 */
	private void build() {
		this.xml = XML.build();
		final Element envelope = this.xml.elementNS(this.xml.document(), "s:Envelope", NAMESPACE_ENVELOPE);
		envelope.setAttributeNS(NAMESPACE_ENVELOPE, "encodingStyle", NAMESPANCE_ENCODING);
		this.body = this.xml.element(envelope, "s:Body");
	}

	/**
	 * <p>外网IP地址</p>
	 * <pre>
	 * {@code
	 * <?xml version="1.0"?>
	 * <s:Envelope xmlns:s="http://schemas.xmlsoap.org/soap/envelope/" s:encodingStyle="http://schemas.xmlsoap.org/soap/encoding/">
	 * 	<s:Body>
	 * 		<u:GetExternalIPAddress xmlns:u="urn:schemas-upnp-org:service:WANIPConnection:1"></u:GetExternalIPAddress>
	 * 	</s:Body>
	 * </s:Envelope>
	 * }
	 * </pre>
	 * 
	 * @return 请求内容
	 */
	public String buildGetExternalIPAddress() {
		this.xml.elementNS(this.body, "u:GetExternalIPAddress", this.serviceType);
		return this.xml();
	}
	
	/**
	 * <p>端口映射信息</p>
	 * <pre>
	 * {@code
	 * <?xml version="1.0"?>
	 * <s:Envelope xmlns:s="http://schemas.xmlsoap.org/soap/envelope/" s:encodingStyle="http://schemas.xmlsoap.org/soap/encoding/">
	 * 	<s:Body>
	 * 		<u:GetSpecificPortMappingEntry xmlns:u="urn:schemas-upnp-org:service:WANIPConnection:1">
	 * 			<NewRemoteHost></NewRemoteHost>
	 * 			<NewExternalPort>8080</NewExternalPort>
	 * 			<NewProtocol>TCP</NewProtocol>
	 * 		</u:GetSpecificPortMappingEntry>
	 * 	</s:Body>
	 * </s:Envelope>
	 * }
	 * </pre>
	 * 
	 * @param portExt 外网端口
	 * @param protocol 协议
	 * 
	 * @return 请求内容
	 */
	public String buildGetSpecificPortMappingEntry(int portExt, Protocol.Type protocol) {
		final Element mapping = this.xml.elementNS(this.body, "u:GetSpecificPortMappingEntry", this.serviceType);
		this.xml.element(mapping, "NewRemoteHost", "");
		this.xml.element(mapping, "NewExternalPort", String.valueOf(portExt));
		this.xml.element(mapping, "NewProtocol", protocol.name().toUpperCase());
		return this.xml();
	}
	
	/**
	 * <p>端口映射</p>
	 * <pre>
	 * {@code
	 * <?xml version="1.0"?>
	 * <s:Envelope xmlns:s="http://schemas.xmlsoap.org/soap/envelope/" s:encodingStyle="http://schemas.xmlsoap.org/soap/encoding/">
	 * 	<s:Body>
	 * 		<u:AddPortMapping xmlns:u="urn:schemas-upnp-org:service:WANIPConnection:1">
	 * 			<NewRemoteHost></NewRemoteHost>
	 * 			<NewExternalPort>8080</NewExternalPort>
	 * 			<NewProtocol>TCP</NewProtocol>
	 * 			<NewInternalPort>8080</NewInternalPort>
	 * 			<NewInternalClient>192.168.1.100</NewInternalClient>
	 * 			<NewEnabled>1</NewEnabled>
	 * 			<NewPortMappingDescription>描述</NewPortMappingDescription>
	 * 			<NewLeaseDuration>0</NewLeaseDuration>
	 * 		</u:AddPortMapping>
	 * 	</s:Body>
	 * </s:Envelope>
	 * }
	 * </pre>
	 * 
	 * @param port 内网端口
	 * @param address 内网地址
	 * @param portExt 外网端口
	 * @param protocol 协议
	 * 
	 * @return 请求内容
	 */
	public String buildAddPortMapping(int port, String address, int portExt, Protocol.Type protocol) {
		final Element mapping = this.xml.elementNS(this.body, "u:AddPortMapping", this.serviceType);
		this.xml.element(mapping, "NewRemoteHost", "");
		this.xml.element(mapping, "NewExternalPort", String.valueOf(portExt));
		this.xml.element(mapping, "NewProtocol", protocol.name().toUpperCase());
		this.xml.element(mapping, "NewInternalPort", String.valueOf(port));
		this.xml.element(mapping, "NewInternalClient", address);
		this.xml.element(mapping, "NewEnabled", "1");
		this.xml.element(mapping, "NewPortMappingDescription", SystemConfig.getNameEn());
		this.xml.element(mapping, "NewLeaseDuration", "0");
		return this.xml();
	}
	
	/**
	 * <p>删除端口映射</p>
	 * <pre>
	 * {@code
	 * <?xml version="1.0" encoding="UTF-8"?>
	 * <s:Envelope xmlns:s="http://schemas.xmlsoap.org/soap/envelope/" s:encodingStyle="http://schemas.xmlsoap.org/soap/encoding/">
	 * 	<s:Body>
	 * 		<u:DeletePortMapping xmlns:u="urn:schemas-upnp-org:service:WANIPConnection:1">
	 * 			<NewRemoteHost></NewRemoteHost>
	 * 			<NewExternalPort>8080</NewExternalPort>
	 * 			<NewProtocol>TCP</NewProtocol>
	 * 		</u:DeletePortMapping>
	 * 	</s:Body>
	 * </s:Envelope>
	 * }
	 * </pre>
	 * 
	 * @param portExt 外网端口
	 * @param protocol 协议
	 * 
	 * @return 请求内容
	 */
	public String buildDeletePortMapping(int portExt, Protocol.Type protocol) {
		final Element mapping = this.xml.elementNS(body, "u:DeletePortMapping", this.serviceType);
		this.xml.element(mapping, "NewRemoteHost", "");
		this.xml.element(mapping, "NewExternalPort", String.valueOf(portExt));
		this.xml.element(mapping, "NewProtocol", protocol.name().toUpperCase());
		return this.xml();
	}

	/**
	 * <p>XML文本输出</p>
	 * 
	 * @return XML文本
	 */
	private String xml() {
		return this.xml.xml(false);
	}

}
