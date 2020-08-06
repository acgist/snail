package com.acgist.snail;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.junit.jupiter.api.Test;

import com.acgist.snail.net.ftp.bootstrap.FtpClientBuilder;
import com.acgist.snail.system.exception.NetException;
import com.acgist.snail.utils.FileUtils;

public class FtpClientTest extends BaseTest {

	@Test
	public void testDownload() throws FileNotFoundException, IOException, NetException {
//		String path = "ftp://user:password@localhost/Snail.exe";
//		String path = "ftp://localhost/elk/elasticsearch-6.4.1.zip";
		String path = "ftp://localhost/VS2012中文旗舰版/vs_ultimate.exe";
		var client = FtpClientBuilder.newInstance(path).build();
		var ok = client.connect();
		if(!ok) {
			this.log("FTP服务器连接失败");
			return;
		}
		this.log("文件大小：" + client.size());
		var input = client.download();
		if(input == null) {
			this.log(client.failMessage("未知错误"));
		} else {
			var output = new FileOutputStream("e:/tmp/" + FileUtils.fileName(path));
			input.transferTo(output);
			output.flush();
			output.close();
		}
		client.close();
		this.log("OK");
	}
	
}
