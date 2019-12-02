package com.acgist.snail;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.acgist.snail.net.http.HTTPClient;
import com.acgist.snail.net.upnp.UpnpServer;
import com.acgist.snail.pojo.wrapper.HeaderWrapper;
import com.acgist.snail.system.exception.NetException;

public class HeaderWrapperTest extends BaseTest {

	private static final String NEW_LINE = "\r\n";
	
	@Test
	public void testBuild() {
		final StringBuilder builder = new StringBuilder();
		builder
			.append("M-SEARCH * HTTP/1.1").append(NEW_LINE)
			.append("HOST: ").append(UpnpServer.UPNP_HOST).append(":").append(UpnpServer.UPNP_PORT).append(NEW_LINE)
			.append("MX: 3").append(NEW_LINE)
			.append("ST: urn:schemas-upnp-org:device:InternetGatewayDevice:1").append(NEW_LINE)
			.append("MAN: \"ssdp:discover\"").append(NEW_LINE)
			.append(NEW_LINE);
		this.log(builder.toString());
		HeaderWrapper wrapper = HeaderWrapper.newBuilder("M-SEARCH * HTTP/1.1");
		wrapper
			.header("HOST", UpnpServer.UPNP_HOST + ":" + UpnpServer.UPNP_PORT)
			.header("MX", "3")
			.header("ST", "urn:schemas-upnp-org:device:InternetGatewayDevice:1")
			.header("MAN", "\"ssdp:discover\"");
		this.log(wrapper.build());
		assertEquals(builder.toString(), wrapper.build());
	}
	
	@Test
	public void testHttp() throws NetException {
		HTTPClient client = HTTPClient.newInstance("https://www.acgist.com/demo/weixin/view");
		var headers = client.head();
		headers.allHeaders().forEach((key, value) -> {
			this.log(key + "<==>" + value);
		});
		this.log(headers.header("SERVER"));
	}
	
	@Test
	public void testBuildMSearch() {
		HeaderWrapper wrapper = HeaderWrapper.newBuilder("M-SEARCH * HTTP/1.1");
		wrapper
			.header("HOST", "1234")
			.header("MX", "3")
			.header("MX", "4");
		this.log(wrapper.build());
	}
	
}
