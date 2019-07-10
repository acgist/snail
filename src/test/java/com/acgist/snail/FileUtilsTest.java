package com.acgist.snail;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.Test;

import com.acgist.snail.net.http.HTTPClient;
import com.acgist.snail.system.exception.NetException;
import com.acgist.snail.system.file.FileRecycle;
import com.acgist.snail.utils.FileUtils;
import com.acgist.snail.utils.StringUtils;

public class FileUtilsTest {

	@Test
	public void fileNameUrl() {
		System.out.println(FileUtils.fileNameFromUrl("http://casd/%e8%ae%a2%e5%8d%95fds.mpe?xx"));
		System.out.println(FileUtils.fileNameFromUrl("https://www.acgist.com/demo/weixin/view?xx?xx"));
	}
	
	@Test
	public void fileNameHttp() throws NetException {
		HTTPClient client = HTTPClient.newInstance("https://www.acgist.com/demo/weixin/view");
		client.head().headers().forEach((key, value) -> {
			System.out.println(key + "-" + value);
		});
	}
	
	@Test
	public void fileType() {
		System.out.println(FileUtils.fileType("http://casd/fds.mpe"));
	}
	
	@Test
	public void size() {
		System.out.println(FileUtils.fileSize("E:\\gitee\\snail\\download\\MY-1.211.0.exe"));
	}
	
	@Test
	public void verify() {
		FileUtils.md5("F:\\迅雷下载\\我的大叔\\[我的大叔][E008].mkv").forEach((key, value) -> {
			System.out.println(value + "=" + key);
		});
		FileUtils.sha1("F:\\迅雷下载\\我的大叔\\[我的大叔][E008].mkv").forEach((key, value) -> {
			System.out.println(value + "=" + key);
		});
	}
	
	@Test
	public void recycle() throws IOException {
		File da = new File("E:/$RECYCLE.BIN/S-1-5-21-1082702080-4186364021-1016170526-1001/$Itest1234.txt");
		File db = new File("E:/$RECYCLE.BIN/S-1-5-21-1082702080-4186364021-1016170526-1001/$Rtest1234.txt");
		da.delete();
		db.delete();
		FileRecycle recycle = new FileRecycle();
		byte[] bytes = recycle.buildInfo("E:\\AA.txt");
		System.out.println(StringUtils.hex(bytes));
		File file = new File("E://AA.txt");
		File info = new File("E:/$RECYCLE.BIN/S-1-5-21-1082702080-4186364021-1016170526-1001/$Itest1234.txt");
		if(file.exists()) {
			FileUtils.write(info.getPath(), bytes);
			boolean ok = file.renameTo(new File("E:/$RECYCLE.BIN/S-1-5-21-1082702080-4186364021-1016170526-1001/$Rtest1234.txt"));
			System.out.println("移动结果：" + ok);
		} else {
			System.out.println("文件不存在");
		}
	}
	
//	02000000000000000000000000000000504ab955fd36d5010a00000045003a005c00630063002e007400780074000000
//	0200000000000000000000000000000040b5013ffd36d5010600000045003a005c00440044000000
	@Test
	public void readInfo() throws IOException {
		byte[] bytes = Files.readAllBytes(Paths.get(URI.create("file:///E:/$RECYCLE.BIN/S-1-5-21-1082702080-4186364021-1016170526-1001/$ITKNWKN.txt")));
		System.out.println(bytes.length);
		System.err.println(StringUtils.hex(bytes));
	}
	
	@Test
	public void changeDate() {
		FileRecycle recycle = new FileRecycle();
		byte[] bytes = recycle.buildInfo("E:\\AA.txt");
		System.out.println(StringUtils.hex(bytes));
		File info = new File("E:/$RECYCLE.BIN/S-1-5-21-1082702080-4186364021-1016170526-1001/$I0W1UDC.txt");
		FileUtils.write(info.getPath(), bytes);
	}
	
	@Test
	public void time() {
		System.out.println(System.currentTimeMillis());
	}
	
	@Test
	public void order() {
		ByteBuffer buffer = ByteBuffer.allocate(2);
		buffer.order(ByteOrder.LITTLE_ENDIAN);
		buffer.putChar('你');
		System.out.println(StringUtils.hex(buffer.array()));
	}
	
}
