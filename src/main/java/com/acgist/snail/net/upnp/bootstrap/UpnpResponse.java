package com.acgist.snail.net.upnp.bootstrap;

import com.acgist.snail.utils.StringUtils;
import com.acgist.snail.utils.XMLUtils;

public class UpnpResponse {

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

}
