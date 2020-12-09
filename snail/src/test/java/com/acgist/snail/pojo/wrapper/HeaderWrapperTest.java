package com.acgist.snail.pojo.wrapper;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.acgist.snail.context.exception.NetException;
import com.acgist.snail.net.http.HTTPClient;
import com.acgist.snail.net.upnp.UpnpServer;
import com.acgist.snail.utils.Performance;

public class HeaderWrapperTest extends Performance {

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
//		HTTPClient client = HTTPClient.newInstance("https://www.acgist.com/demo/weixin/view");
		HTTPClient client = HTTPClient.newInstance("https://youku.com-youku.com/20180122/OgFJZjkT/900kb/hls/BM3D1t5288298.ts");
		var headers = client.head();
		headers.allHeaders().forEach((key, value) -> {
			this.log(key + "<==>" + value);
		});
		this.log(headers.header("SERVER"));
		this.log(headers.header("Accept-Ranges"));
		this.log(headers.header("Content-Range"));
		this.log(headers.header("Content-Length"));
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

	@Test
	public void testWriter() {
		HeaderWrapper wrapper = HeaderWrapper.newBuilder("GET /");
		wrapper.header("test", null);
		wrapper.header("test", "33 ");
		this.log(wrapper.build());
	}
	
	@Test
	public void testReader() {
		HeaderWrapper wrapper = HeaderWrapper.newInstance("GET /\n"
			+ "TEST: TEST \n"
			+ "TEST2:  TEST2  \n"
			+ "TEST3: TEST 1\n"
			+ "TEST3:TEST 3 \n"
			+ "TEST6: \n"
		);
		this.log(wrapper.protocol());
		this.log(wrapper.header("test"));
		this.log(wrapper.header("test2"));
		this.log(wrapper.header("test3"));
		this.log(wrapper.headerList("test3"));
		this.log(wrapper.header("test4"));
		this.log(wrapper.headerList("test5"));
		this.log(wrapper.header("test6"));
		this.log(wrapper.headerList("test6"));
	}
	
}
