package com.acgist.snail;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.junit.Test;

import com.acgist.snail.net.ftp.bootstrap.FtpClientBuilder;
import com.acgist.snail.system.exception.NetException;

public class FtpClientTest extends BaseTest {

	@Test
	public void download() throws FileNotFoundException, IOException, NetException {
//		var client = FtpClientBuilder.newInstance("ftp://localhost/elk/elasticsearch-6.4.1.zip").build();
		var client = FtpClientBuilder.newInstance("ftp://localhost/VS2012中文旗舰版/vs_ultimate.exe").build();
		var ok = client.connect();
		if(!ok) {
			this.log("FTP服务器连接失败");
			return;
		}
		this.log(client.size());
		var input = client.download();
		if(input == null) {
			this.log(client.failMessage("未知错误"));
		} else {
			var output = new FileOutputStream("e://ftp");
			input.transferTo(output);
			output.flush();
			output.close();
		}
		client.close();
		this.log("OK");
	}
	
}
