package com.acgist.snail.net.ftp;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.junit.jupiter.api.Test;

import com.acgist.snail.context.exception.NetException;
import com.acgist.snail.utils.FileUtils;
import com.acgist.snail.utils.Performance;

public class FtpClientTest extends Performance {

	@Test
	public void testDownload() throws FileNotFoundException, IOException, NetException {
		if(SKIP) {
			this.log("跳过FTP测试");
			return;
		}
		final String path = "ftp://localhost/ftp/中文文件.exe";
//		final String path = "ftp://localhost/ftp/FTPserver.exe";
//		final String path = "ftp://demo:password@test.rebex.net/readme.txt";
		final var client = FtpClient.newInstance(path);
		final var success = client.connect();
		if(!success) {
			this.log("FTP服务器连接失败");
			return;
		}
		this.log("文件大小：" + client.size());
		final var input = client.download();
		final String target = "E:/tmp/" + FileUtils.fileName(path);
		final var output = new FileOutputStream(target);
		input.transferTo(output);
		output.flush();
		output.close();
		client.close();
		this.log("OK");
		final File targetFile = new File(target);
		assertTrue(targetFile.exists());
		assertTrue(targetFile.delete());
	}
	
}
