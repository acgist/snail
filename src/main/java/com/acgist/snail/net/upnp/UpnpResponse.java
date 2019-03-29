package com.acgist.snail.net.upnp;

import com.acgist.snail.utils.StringUtils;
import com.acgist.snail.utils.XMLUtils;

public class UpnpResponse {

	private static final String SERVICE_ID = "urn:upnp-org:serviceId:WANIPConnection"; // 服务ID
	
//	返回报文：
//	<?xml version="1.0"?>
//	<SOAP-ENV:Envelope xmlns:SOAP-ENV="http://schemas.xmlsoap.org/soap/envelope/" SOAP-ENV:encodingStyle="http://schemas.xmlsoap.org/soap/encoding/">
//		<SOAP-ENV:Body>
//			<u:GetExternalIPAddressResponse xmlns:u="urn:schemas-upnp-org:service:WANIPConnection:1">
//				<NewExternalIPAddress>219.137.140.38</NewExternalIPAddress>
//			</u:GetExternalIPAddressResponse>
//		</SOAP-ENV:Body>
//	</SOAP-ENV:Envelope>
	/**
	 * 解析获取外网IP响应
	 */
	public static final String parseGetExternalIPAddress(String body) {
		if(StringUtils.isEmpty(body)) {
			return null;
		}
		XMLUtils xml = XMLUtils.load(body);
		return xml.elementValue("NewExternalIPAddress");
	}
	
//	返回报文：
//	<?xml version="1.0"?>
//	<SOAP-ENV:Envelope xmlns:SOAP-ENV="http://schemas.xmlsoap.org/soap/envelope/" SOAP-ENV:encodingStyle="http://schemas.xmlsoap.org/soap/encoding/">
//		<SOAP-ENV:Body>
//			<u:GetSpecificPortMappingEntryResponse xmlns:u="urn:schemas-upnp-org:service:WANIPConnection:1">
//				<NewInternalPort>80</NewInternalPort><!-- 映射内网端口 -->
//				<NewInternalClient>192.168.1.100</NewInternalClient><!-- 映射内网端口 -->
//				<NewEnabled>1</NewEnabled>
//				<NewPortMappingDescription>JUAN UPNP</NewPortMappingDescription>
//				<NewLeaseDuration>0</NewLeaseDuration>
//			</u:GetSpecificPortMappingEntryResponse>
//		</SOAP-ENV:Body>
//	</SOAP-ENV:Envelope>
	/**
	 * 获取端口映射情况：不解析，直接判断状态
	 */
	public static final boolean parseGetSpecificPortMappingEntry() {
		return false;
	}
	
//	返回报文：
//	<?xml version="1.0"?>
//	<SOAP-ENV:Envelope xmlns:SOAP-ENV="http://schemas.xmlsoap.org/soap/envelope/" SOAP-ENV:encodingStyle="http://schemas.xmlsoap.org/soap/encoding/">
//		<SOAP-ENV:Body>
//			<u:AddPortMappingResponse xmlns:u="urn:schemas-upnp-org:service:WANIPConnection:1"></u:AddPortMappingResponse>
//		</SOAP-ENV:Body>
//	</SOAP-ENV:Envelope>
	/**
	 * 添加端口映射：不解析，直接判断状态
	 */
	public static final boolean parseAddPortMapping() {
		return false;
	}

}
