package com.acgist.snail.pojo.wrapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.io.UnsupportedEncodingException;
import java.net.http.HttpHeaders;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.acgist.snail.context.exception.NetException;
import com.acgist.snail.net.http.HTTPClient;
import com.acgist.snail.utils.Performance;

public class HttpHeaderWrapperTest extends Performance {

	@Test
	public void testFileName() throws NetException {
//		final var header = HTTPClient.newInstance("https://g18.gdl.netease.com/MY-1.246.1.apk").head();
//		final var header = HTTPClient.newInstance("http://share.qiniu.easepan.xyz/tool/7tt_setup.exe").head();
//		final var header = HTTPClient.newInstance("https://g37.gdl.netease.com/onmyoji_setup_9.4.0.zip").head();
		// 测试中文名称
		final var header = HTTPClient.newInstance("https://xact02.baidupcs.com/file/567b340cdoa8420596f4421735e7ee00?bkt=en-306f50e23c1196a5660bb403208d4911fd335ce79effdf919ac66775fedeb2fabd62719b3bb90b99bc733a66139fceb5764f6ea6a0ee84cbc40bdc391395b008&fid=604633866-250528-985788120148883&time=1609999137&sign=FDTAXUGERQlBHSKfWqi-DCb740ccc5511e5e8fedcff06b081203-xsbVlQUiDsMc4geuKUaYBbMXJQg%3D&to=126&size=2070270&sta_dx=2070270&sta_cs=180&sta_ft=pdf&sta_ct=5&sta_mt=5&fm2=MH%2CXian%2CAnywhere%2C%2Cguangdong%2Cct&ctime=1599462074&mtime=1599463729&resv0=0&resv1=0&resv2=rlim&resv3=5&resv4=2070270&vuk=604633866&iv=0&htype=&randtype=&newver=1&newfm=1&secfm=1&flow_ver=3&pkey=en-577a5c82f2913c6e16de9cee15a0219d1a8d3c488401f7fd5bcf42bbfc4f9dbf7268bcf34945f41a3de55dde5d4730f933f79e81e7351061305a5e1275657320&expires=8h&rt=pr&r=334262542&vbdid=3309921758&fin=P8%E5%A4%A7%E7%BA%B2.pdf&fn=P8%E5%A4%A7%E7%BA%B2.pdf&rtype=1&dp-logid=152669318311093479&dp-callid=0.1&hps=1&tsl=0&csl=0&fsl=0&csign=J2QRfxxlNsTQqq%2FaLqldKAkd548%3D&so=0&ut=6&uter=4&serv=0&uc=2040240584&ti=05df9239daa40647bedf8e6ef084c28861c84246fa6b4d35&hflag=30&adg=c_0136f02cd5b6ad2e34df548f3b9ed590&reqlabel=250528_f_8c9fc38f403354daf58e5e3afb6cb1f2_-1_b3df4456edd1432440b58376073ad2f6&by=themis").head();
		this.log(header);
		final String defaultName = "test";
		final String fileName = header.fileName("test");
		this.log(fileName);
		assertNotEquals(defaultName, fileName);
	}
	
	@Test
	public void fileName() throws UnsupportedEncodingException {
		var headers = HttpHeaders.of(Map.of("Content-Disposition", List.of("attachment;filename='snail.jar'")), (a, b) -> true);
		HttpHeaderWrapper headerWrapper = HttpHeaderWrapper.newInstance(headers);
		String filename = headerWrapper.fileName("错误");
		this.log(filename);
		assertEquals(filename, "snail.jar");
		headers = HttpHeaders.of(Map.of("Content-Disposition", List.of("attachment;filename='%e6%b5%8b%e8%af%95.exe'")), (a, b) -> true);
		headerWrapper = HttpHeaderWrapper.newInstance(headers);
		filename = headerWrapper.fileName("错误");
		this.log(filename);
		assertEquals(filename, "测试.exe");
		headers = HttpHeaders.of(Map.of("Content-Disposition", List.of("attachment;filename='" + new String("测试.exe".getBytes("GBK"), "ISO-8859-1") + "'")), (a, b) -> true);
		headerWrapper = HttpHeaderWrapper.newInstance(headers);
		filename = headerWrapper.fileName("错误");
		this.log(filename);
		assertEquals(filename, "测试.exe");
		headers = HttpHeaders.of(Map.of("Content-Disposition", List.of("attachment;filename=\"snail.jar\"")), (a, b) -> true);
		headerWrapper = HttpHeaderWrapper.newInstance(headers);
		filename = headerWrapper.fileName("错误");
		this.log(filename);
		assertEquals(filename, "snail.jar");
		headers = HttpHeaders.of(Map.of("Content-Disposition", List.of("attachment;filename=snail.jar?version=1.0.0")), (a, b) -> true);
		headerWrapper = HttpHeaderWrapper.newInstance(headers);
		filename = headerWrapper.fileName("错误");
		this.log(filename);
		assertEquals(filename, "snail.jar");
		headers = HttpHeaders.of(Map.of("Content-Disposition", List.of("inline;filename=\"snail.jar\";filename*=utf-8''snail.jar")), (a, b) -> true);
		headerWrapper = HttpHeaderWrapper.newInstance(headers);
		filename = headerWrapper.fileName("错误");
		this.log(filename);
		assertEquals(filename, "snail.jar");
	}
	
}
