package com.acgist.snail.net.upnp.bootstrap;

import com.acgist.snail.utils.StringUtils;
import com.acgist.snail.utils.XMLUtils;

/**
 * UPNP响应
 * 
 * @author acgist
 * @since 1.0.0
 */
public final class UpnpResponse {

	/**
	 * <p>外网IP地址响应</p>
	 * <pre>
	 * <xmp>
<?xml version="1.0"?>
<s:Envelope xmlns:s="http://schemas.xmlsoap.org/soap/envelope/" s:encodingStyle="http://schemas.xmlsoap.org/soap/encoding/">
	<s:Body>
		<u:GetExternalIPAddressResponse xmlns:u="urn:schemas-upnp-org:service:WANIPConnection:1">
			<NewExternalIPAddress>219.137.140.38</NewExternalIPAddress>
		</u:GetExternalIPAddressResponse>
	</s:Body>
</s:Envelope>
	 * </xmp>
	 * </pre>
	 */
	public static final String parseGetExternalIPAddress(String body) {
		if(StringUtils.isEmpty(body)) {
			return null;
		}
		final XMLUtils xml = XMLUtils.load(body);
		return xml.elementValue("NewExternalIPAddress");
	}
	
	/**
	 * <p>端口映射信息响应</p>
	 * <pre>
	 * <xmp>
<?xml version="1.0"?>
<s:Envelope xmlns:s="http://schemas.xmlsoap.org/soap/envelope/" s:encodingStyle="http://schemas.xmlsoap.org/soap/encoding/">
	<s:Body>
		<u:GetSpecificPortMappingEntryResponse xmlns:u="urn:schemas-upnp-org:service:WANIPConnection:1">
			<NewInternalPort>17888</NewInternalPort>
			<NewInternalClient>192.168.1.100</NewInternalClient>
			<NewEnabled>1</NewEnabled>
			<NewPortMappingDescription>Snail</NewPortMappingDescription>
			<NewLeaseDuration>0</NewLeaseDuration>
		</u:GetSpecificPortMappingEntryResponse>
	</s:Body>
</s:Envelope>
	 * </xmp>
	 * </pre>
	 * 
	 * @return 端口映射的内网IP地址
	 */
	public static final String parseGetSpecificPortMappingEntry(String body) {
		if(StringUtils.isEmpty(body)) {
			return null;
		}
		final XMLUtils xml = XMLUtils.load(body);
		return xml.elementValue("NewInternalClient");
	}

}
