package com.acgist.snail;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.junit.Test;

import com.acgist.snail.net.http.HTTPClient;
import com.acgist.snail.system.exception.NetException;
import com.acgist.snail.utils.FileUtils;
import com.acgist.snail.utils.StringUtils;

public class FileUtilsTest extends BaseTest {
	
	@Test
	public void fileNameUrl() {
		this.log(FileUtils.fileNameFromUrl("http://casd/%e8%ae%a2%e5%8d%95fds.mpe?xx"));
		this.log(FileUtils.fileNameFromUrl("https://www.acgist.com/demo/weixin/view?xx?xx"));
	}
	
	@Test
	public void fileNameHttp() throws NetException {
		HTTPClient client = HTTPClient.newInstance("https://www.acgist.com/demo/weixin/view");
		var headers = client.head();
		headers.allHeaders().forEach((key, value) -> {
			this.log(key + "<---->" + value);
		});
		this.log(headers.header("SERVER"));
	}
	
	@Test
	public void fileType() {
		this.log(FileUtils.fileType("http://casd/fds.mp4"));
		this.log(FileUtils.fileType("http://casd/fds.JPEG"));
	}
	
//	@Test
//	public void fileTypeSort() {
//		FileUtils.FILE_TYPE_EXT.forEach((key, value) -> {
//			this.log(key);
//			var list = new ArrayList<>(value);
//			Collections.sort(list);
//			this.log("\"" + String.join("\", \"", list) + "\"");
//		});
//	}
	
	@Test
	public void size() {
		this.log(FileUtils.fileSize("E:\\gitee\\snail\\download\\MY-1.211.0.exe"));
	}
	
	@Test
	public void verify() {
		FileUtils.md5("F:\\迅雷下载\\我的大叔\\[我的大叔][E008].mkv").forEach((key, value) -> {
			this.log(value + "=" + key);
		});
		FileUtils.sha1("F:\\迅雷下载\\我的大叔\\[我的大叔][E008].mkv").forEach((key, value) -> {
			this.log(value + "=" + key);
		});
	}
	
	/**
	 * JAVA默认大端
	 */
	@Test
	public void order() {
		this.log(ByteOrder.nativeOrder());
		ByteBuffer buffer = ByteBuffer.allocate(2);
		buffer.order(ByteOrder.LITTLE_ENDIAN);
		buffer.putChar('你');
		this.log(StringUtils.hex(buffer.array()));
		buffer = ByteBuffer.allocate(2);
		buffer.putChar('你');
		this.log(StringUtils.hex(buffer.array()));
		buffer = ByteBuffer.allocate(2);
		buffer.order(ByteOrder.BIG_ENDIAN);
		buffer.putChar('你');
		this.log(StringUtils.hex(buffer.array()));
	}
	
}
